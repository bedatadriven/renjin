/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.eval;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.invoke.annotations.CompilerSpecialization;
import org.renjin.pipeliner.VectorPipeliner;
import org.renjin.primitives.Primitives;
import org.renjin.primitives.Warning;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.primitives.special.ControlFlowException;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contexts are the internal mechanism used to keep track of where a
 * computation has got to (and from where),
 * so that control-flow constructs can work and reasonable information
 * can be produced on error conditions,
 * (such as via traceback) and otherwise (the sys.xxx functions).
 */
public class Context {
  

  public enum Type {
    /** toplevel context */
    TOP_LEVEL,

    /** target for next */
    NEXT,

    /** target for break */
    BREAK,

    /** break or next target */
    LOOP,

    /** function closure */
    FUNCTION,

    /** other functions that need error cleanup */
    CCODE,

    /** return() from a closure */
    RETURN,

    /** return target on exit from browser */
    BROWSER,

    /** rather, running an S3 method */
    GENERIC,

    /** a call to restart was made from a closure */
    RESTART,

    /** builtin internal function */
    BUILTIN
  }

  private List<SEXP> onExit = Lists.newArrayList();
  private List<SEXP> restarts = null;

  private Context parent;
  private int evaluationDepth;
  private Type type;
  private Environment environment;
  private Session session;
  private FunctionCall call;
  private SEXP function;

  /**
   * The environment from which the closure was called
   */
  private Environment callingEnvironment;
  
  private PairList arguments = Null.INSTANCE;

  /**
   * Conditions are analogous to Exceptions.
   * Handlers are R functions that are called immediately when
   * conditions are signaled.
   */
  private Map<String, ConditionHandler> conditionHandlers = null;

  private Map<Class, Object> stateMap = null;

  private Context() {
  }

  /**
   *
   * @return a new top level context using the default VFS FileSystemManager and the
   * renjin-core jar as the R_HOME directory.
   *
   * @see org.apache.commons.vfs2.VFS#getManager()
   * @see org.renjin.util.FileSystemUtils#homeDirectoryInCoreJar(FileSystemManager)
   */
  public static Context newTopLevelContext() {
    return SessionBuilder.buildDefault().getTopLevelContext();
  }

  
  Context(Session session) {
    this.session = session;
    this.type = Type.TOP_LEVEL;
    this.environment = session.getGlobalEnvironment();
    this.function = Null.INSTANCE;
  }

  public Context beginFunction(Environment callingEnvironment, FunctionEnvironment callEnvironment, FunctionCall call, Closure closure) {
    Context context = new Context();
    context.type = Type.FUNCTION;
    context.parent = this;
    context.evaluationDepth = evaluationDepth+1;
    context.function = closure;
    context.environment = callEnvironment;
    context.session = session;
    context.call = call;
    context.callingEnvironment = callingEnvironment;
    return context;
  }

  public Context beginFunction(Environment rho, FunctionCall call, Closure closure, PairList arguments) {
    assert rho != null : "callingEnvironment cannot be null.";
    Context context = new Context();
    context.type = Type.FUNCTION;
    context.parent = this;
    context.evaluationDepth = evaluationDepth+1;
    context.function = closure;
    context.environment = new FunctionEnvironment(closure.getEnclosingEnvironment(), closure.getFormals());
    context.session = session;
    context.arguments = arguments;
    context.call = call;
    context.callingEnvironment = rho;
    return context;
  }
  
  public Context beginEvalContext(Environment environment) {
    Context context = new Context();
    context.type = Type.RETURN;
    context.parent = this;
    context.evaluationDepth = evaluationDepth+1;
    context.environment = environment;
    context.session = session;
    context.function = Primitives.getInternal(Symbol.get("eval"));

    // Use the call from the call to the eval wrapper
    context.call = this.call;

    return context;
  }

  /**
   *
   * @return the context in which to start searching for condition handlers.
   */
  public Context getConditionStack() {
    Context stack = this.session.conditionStack;
    if(stack == null) {
      stack = this;
    }
    return stack;
  }

  /**
   * Evaluates the given calling handler function call in this context, but temporarily sets
   * the conditionStack pointer to the parent of the context in which the handler was defined.
   *
   * <p>This ensures that a signal can be rethrown within the condition handler and not be caught
   * infinitely.</p>
   *
   * @param definitionContext the context in which the condition handler was defined.
   * @param handlerCall the function call to evaluate.
   */
  public SEXP evaluateCallingHandler(Context definitionContext, SEXP handlerCall) {
    this.session.conditionStack = definitionContext.getParent();
    try {
      return evaluate(handlerCall);
    } finally {
      this.session.conditionStack = null;
    }
  }
  public SEXP evaluate(SEXP expression) {
    SEXP result = evaluate(expression, environment);
    if(result == null) {
      throw new IllegalStateException("Evaluated to null");
    }
    return result;
  }
  
  /**
   * If the S-Expression is an {@code DeferredComputation}, then it is executed with the
   * VectorPipeliner.
   * @param sexp
   * @return
   */
  public SEXP materialize(SEXP sexp) {
    if(sexp instanceof Vector) {
      Vector vector = (Vector) sexp;
      if(vector.isDeferred() && !vector.isConstantAccessTime()) {
        return session.getVectorEngine().materialize(vector);
      }
    }
    return sexp;
  }

  public ListVector materialize(ListVector listVector) {
    if(!anyDeferred(listVector)) {
      return listVector;
    }

    return session.getVectorEngine().materialize(listVector);
  }

  private boolean anyDeferred(ListVector listVector) {
    for (int i = 0; i < listVector.length(); i++) {
      SEXP element = listVector.getElementAsSEXP(i);
      if(element instanceof Vector) {
        Vector vector = (Vector) element;
        if(vector.isDeferred() && !vector.isConstantAccessTime()) {
          return true;
        }
      }
    }
    return false;
  }
  
  public Vector materialize(Vector sexp) {
    if(sexp.isDeferred() && !sexp.isConstantAccessTime()) {
      return session.getVectorEngine().materialize(sexp);
    } else {
      return sexp;
    }
  }

  public SEXP simplify(SEXP sexp) {
    if(sexp instanceof MemoizedComputation && ((MemoizedComputation) sexp).isCalculated()) {
      return sexp;
    }

    if(sexp instanceof DeferredComputation &&
        ((DeferredComputation) sexp).getComputationDepth() > VectorPipeliner.MAX_DEPTH) {
      return session.getVectorEngine().simplify((DeferredComputation) sexp);
    } else {
      return sexp;
    }
  }

  public SEXP evaluate(SEXP expression, Environment rho) {
    return evaluate(expression, rho, false);
  }

  /**
   * Evaluates the given {@code expression} in the given {@code environment}.
   *
   * @param expression the expression to evaluate
   * @param rho the environment in which to evaluate the expression
   * @param allowMissing if {@code true}, missing arguments without defaults should evaluate to {@code Symbol.MISSING_ARG},
   *                     otherwise they will result in an EvalException.
   * @return the result of the evaluation.
   */
  public SEXP evaluate(SEXP expression, Environment rho, boolean allowMissing) {
    if(expression instanceof Symbol) {
      return evaluateSymbol((Symbol) expression, rho, allowMissing);
    } else if(expression instanceof ExpressionVector) {
      return evaluateExpressionVector((ExpressionVector) expression, rho);
    } else if(expression instanceof FunctionCall) {
      return evaluateCall((FunctionCall) expression, rho);
    } else if(expression instanceof Promise) {
      return expression.force(this);
    } else if(expression != Null.INSTANCE && expression instanceof PromisePairList) {
      throw new EvalException("'...' used in an incorrect context");
    } else {
      clearInvisibleFlag();
      return expression;
    }
  }

  public <T> T getState(Class<T> clazz) {
    if(stateMap != null) {
      return (T)stateMap.get(clazz);
    } else {
      return null;
    }
  }

  public void clearState(Class<?> stateType) {
    stateMap.remove(stateType);
  }

  public <T> T getSingleton(Class<T> clazz) {
    return session.getSingleton(clazz);
  }

  public <T> void setState(T instance) {
    this.setState((Class<T>) instance.getClass(), instance);
  }

  public <T> void setState(Class<T> clazz, T instance) {
    if(stateMap == null) {
      stateMap = Maps.newHashMap();
    }
    stateMap.put(clazz, instance);
  }

  private SEXP evaluateSymbol(Symbol symbol, Environment rho, boolean allowMissing) {
    clearInvisibleFlag();

    if(symbol == Symbol.MISSING_ARG) {
      return symbol;
    }

    if(allowMissing && symbol.isVarArgReference()) {
      if(rho.findVariable(this, Symbols.ELLIPSES) == Null.INSTANCE) {
        return Symbol.MISSING_ARG;
      }
    }

    SEXP value = rho.findVariable(this, symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException(String.format("object '%s' not found", symbol.getPrintName()));
    }

    if(!allowMissing) {
      if (value == Symbol.MISSING_ARG) {
        throw new EvalException("argument '%s' is missing, with no default", symbol.getPrintName());
      }
    }

    if(value instanceof Promise) {
      value = value.force(this, allowMissing);
    }

    return value;
  }

  /**
   * Adds a restart object by name to this Context.
   */
  public void addRestart(SEXP restartObject) {
    if(restarts == null) {
      restarts = new ArrayList<>();
    }
    restarts.add(restartObject);
  }

  /**
   * Tries to find a restart in this context or
   * one of it's parents.
   *
   * @param index the zero-based index of the restart to find. Index "0" is the last added restart
   *            in this context or its nearest parent.
   */
  public SEXP getRestart(int index) {
    Context context = this;
    while(!context.isTopLevel()) {
      if(context.restarts != null) {
        for (int i = 0; i < context.restarts.size(); i++, index--) {
          if(index == 0) {
            return context.restarts.get(i);
          }
        }
      }
      context = context.getParent();
    }
    return Null.INSTANCE;
  }
  
  private SEXP evaluateExpressionVector(ExpressionVector expressionVector, Environment rho) {
    if(expressionVector.length() == 0) {
      setInvisibleFlag();
      return Null.INSTANCE;
    } else {
      SEXP result = Null.INSTANCE;
      for(SEXP sexp : expressionVector) {
        result = evaluate(sexp, rho);
      }
      return result;
    }
  }

  public SEXP evaluateCall(FunctionCall call, Environment rho) {
    clearInvisibleFlag();

    SEXP fn = call.getFunction();
    Function functionExpr = evaluateFunction(rho, fn);

    boolean profiling = Profiler.ENABLED && fn instanceof Symbol && !((Symbol) fn).isReservedWord();
    if(Profiler.ENABLED && profiling) {
      Profiler.functionStart((Symbol)fn, functionExpr);
    }

    List<String> argumentNames = new ArrayList<>();
    List<SEXP> arguments = new ArrayList<>();

    for (PairList.Node node : call.getArguments().nodes()) {
      SEXP value = node.getValue();
      if(value == Symbols.ELLIPSES && !(functionExpr instanceof SpecialFunction)) {
        SEXP expando = rho.getEllipsesVariable();
        if(expando == Symbol.UNBOUND_VALUE) {
          throw new EvalException("'...' used in an incorrect context");
        }
        if(expando instanceof PromisePairList) {
          PromisePairList extra = (PromisePairList) expando;
          for (PairList.Node extraNode : extra.nodes()) {
            argumentNames.add(extraNode.hasTag() ? extraNode.getName() : null);
            arguments.add(extraNode.getValue());
          }
        }

      } else {
        if(node.hasName()) {
          argumentNames.add(node.getTag().getPrintName());
        } else {
          argumentNames.add(null);
        }
        if(value == Symbol.MISSING_ARG) {
          arguments.add(Symbol.MISSING_ARG);
        } else {
          arguments.add(Promise.repromise(rho, value));
        }
      }
    }

    try {
      return functionExpr.apply(this, rho, call, argumentNames.toArray(new String[0]), arguments.toArray(new SEXP[0]), null);
    } catch (EvalException | ControlFlowException | ConditionException | Error e) {
      throw e;

    } catch (Exception e) {
      String message = e.getMessage();
      if(message == null) {
        message = e.getClass().getName();
      }
      throw new EvalException(message, e);
      
    } finally {
      if(Profiler.ENABLED && profiling) {
        Profiler.functionEnd();
      }
    }
  }

  /**
   * Evaluates a function reference from compiled code.
   */
  @CompilerSpecialization
  public Function evaluateFunction(Environment rho, String name) {
    Function function = rho.findFunction(this, Symbol.get(name));
    if(function == null) {
      throw new EvalException("Could not find function \"%s\"", name);
    }
    return function;
  }

  private Function evaluateFunction(Environment rho, SEXP functionExp) {
    if(functionExp instanceof Symbol) {
      Symbol symbol = (Symbol) functionExp;
      if(symbol.isReservedWord()) {
        return Primitives.getReservedBuiltin(symbol);
      }
      Function fn = rho.findFunction(this, symbol);
      if(fn == null) {
        throw new EvalException("could not find function '%s'", symbol.getPrintName());      
      }
      return fn;
    } else {
      SEXP evaluated = evaluate(functionExp, rho).force(this);
      if(!(evaluated instanceof Function)) {
        throw new EvalException("'function' of lang expression is of unsupported type '%s'", evaluated.getTypeName());
      }
      return (Function)evaluated;
    }
  }

  /**
   *
   * @return the {@link FileSystemManager} associated with this Context. All R primitives that
   * interact with the file system defer to this manager.
   */
  public FileSystemManager getFileSystemManager() {
    return session.getFileSystemManager();
  }


  /**
   * @see Session#resolveFile(String)
   */
  public FileObject resolveFile(String uri) throws FileSystemException {
    return session.resolveFile(uri);
  }

  /**
   * @return the environment associated with this {@code Context}. This will be
   * either the global environment for top-level contexts
   */
  public Environment getEnvironment() {
    return environment;
  }

  public Environment getGlobalEnvironment() {
    return session.getGlobalEnvironment();
  }

  public SEXP getFunction() {
    return function;
  }

  /**
   * The effective arguments to this function call, promised in the calling environment.
   *
   * Important: in the course of S3 or S4 calls, these arguments may be transformed and can be substantially
   * different than the {@code context.getCall().getArguments()}
   *
   * @return a pairlist containing the arguments to this function context, promised in the calling environment.
   */
  public PairList getArguments() {
    if(type != Type.FUNCTION) {
      throw new IllegalStateException("Only Contexts of type FUNCTION contain a FunctionCall");
    }
    return arguments;
  }

  public SEXP getFunctionName() {
    return call.getFunction();
  }

  /**
   *
   * @return the symbol call S-expression that initiated this function call context.
   */
  public FunctionCall getCall() {
    return call;
  }

  public int getEvaluationDepth() {
    return evaluationDepth;
  }
  
  public int getFrameDepth() {
    int nframe = 0;
    Context cptr = this;
    while (!cptr.isTopLevel()) {
      if (cptr.getType() == Type.FUNCTION ) {
        nframe++;
      }
      cptr = cptr.getParent();
    }
    return nframe;
  }

  public Context getParent() {
    return parent;
  }

  public Session getSession() {
    return session;
  }

  public Type getType() {
    return type;
  }

  public boolean isTopLevel() {
    return parent == null;
  }

  /**
   * Sets an expression to evaluate upon exiting this context,
   * removing any previously added exit expressions.
   * "on.exit" expressions generally do things like close file connections
   * or delete temporary files, often what might be found in a Java {@code finally} clause.
   *
   * @param exp the expression to evaluate upon exiting this context.
   */
  public void setOnExit(SEXP exp) {
    onExit = Lists.newArrayList(exp);
  }

  /**
   * Adds an expression to evaluate upon exiting this context.
   *  "on.exit" expressions generally do things like close file connections
   * or delete temporary files, often what might be found in a Java {@code finally} clause.
   *
   * @param exp the expression to evaluate upon exiting this context.
   */
  public void addOnExit(SEXP exp) {
    onExit.add(exp);
  }


  public void warn(String message) {
    Warning.warning(this, Null.INSTANCE, false, message);
  }

  public void warn(FunctionCall call, String message) {
    Warning.warning(this, call, false, message);
  }
  
  /**
   * Removes all previously added expressions to evaluate upon exiting this context.
   */
  public void clearOnExits() {
    onExit = Lists.newArrayList();
  }

  /**
   * Invokes any on.exit expressions that have been set.
   */
  public void exit() {
    for(SEXP exp : onExit) {
      evaluate(exp, environment);
    }
  }

  /**
   * Sets a function to handle a specific class of condition.
   * @see org.renjin.primitives.Conditions
   *
   * @param conditionClass  the (S3) condition class to be handled by this handler.
   * @param function a function that accepts a signaled
   * condition as an argument.
   * @param calling true if this is a "calling" handler and should be invoked in the {@code Context}
   *                 in which the condition is signaled, {@code false} if control should first
   *                 be returned to the {@code Context} in which it was registered.
   */
  public void setConditionHandler(String conditionClass, SEXP function, boolean calling) {
    if(conditionHandlers == null) {
      conditionHandlers = new HashMap<>();
    }
    conditionHandlers.put(conditionClass, new ConditionHandler(function, calling));
  }

  public ConditionHandler getConditionHandler(String conditionClass) {
    if(conditionHandlers == null) {
      return null;
    }
    return conditionHandlers.get(conditionClass);
  }

  /**
   * Executes the default the standard R initialization sequence:
   * <ol>
   *  <li>Load the base package (/org/renjin/library/base/R/base)</li>
   *  <li>Execute the system profile (/org/renjin/library/base/R/Rprofile)</li>
   *  <li>Evaluate .OptRequireMethods()</li>
   *  <li>Evaluate .First.Sys()</li>
   * </ol>
   *
   */
  public void init() throws IOException {
    evaluate(FunctionCall.newCall(Symbol.get(".onLoad")), session.getBaseNamespaceEnv());
  }

  public void setInvisibleFlag() {
    session.invisible = true;
  }
  
  public void clearInvisibleFlag() {
    session.invisible = false;
  }

  public Environment getCallingEnvironment() {
    return callingEnvironment;
  }

  public Environment getBaseEnvironment() {
    return session.getBaseEnvironment();
  }

  public NamespaceRegistry getNamespaceRegistry() {
    return session.getNamespaceRegistry();
  }

  public void setGlobalVariable(Context context, Symbol symbol, Object value) {
    context.getGlobalEnvironment().setVariable(context, symbol, (SEXP) value);
  }

  public void setGlobalVariable(Context context, String name, Object value) {
    setGlobalVariable(context, Symbol.get(name), value);
  }
}
