/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.eval;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.base.BaseFrame;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.renjin.util.CDefines.*;

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

  private Context parent;
  private int evaluationDepth;
  private Type type;
  private Environment environment;
  private Session session; 
  private FunctionCall call;
  private Closure closure;
  private SEXP    srcRef = Null.INSTANCE;
  private SEXP    srcFile = Null.INSTANCE;
  
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
  private Map<String, SEXP> conditionHandlers = Maps.newHashMap();

  private Map<Class, Object> stateMap = null;

  private Context() {
  }

  /**
   *
   * @return a new top level context using the default VFS FileSystemManager and the
   * renjin-core jar as the R_HOME directory.
   *
   * @see org.apache.commons.vfs2.VFS#getManager()
   * @see org.renjin.util.FileSystemUtils#homeDirectoryInCoreJar()
   */
  public static Context newTopLevelContext() {
    return SessionBuilder.buildDefault().getTopLevelContext();
  }

  
  Context(Session session) {
    this.session = session;
    this.type = Type.TOP_LEVEL;
    this.environment = session.getGlobalEnvironment();
  }

  public Context beginFunction(Environment rho, FunctionCall call, Closure closure, PairList arguments) {
    Context context = new Context();
    context.type = Type.FUNCTION;
    context.parent = this;
    context.evaluationDepth = evaluationDepth+1;
    context.closure = closure;
    context.environment = Environment.createChildEnvironment(closure.getEnclosingEnvironment());
    context.session = session;
    context.arguments = arguments;
    context.call= call;
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
    return context;
  }
  
  public SEXP evaluate(SEXP expression) {
    return evaluate(expression, environment);
  }
  
  /**
   * If the S-Expression is an {@code DeferredComputation}, then it is executed with the
   * VectorPipeliner.
   * @param sexp
   * @return
   */
  public SEXP materialize(SEXP sexp) {
    if(sexp instanceof DeferredComputation && !((DeferredComputation) sexp).isConstantAccessTime()) {
      return session.getVectorEngine().materialize((DeferredComputation)sexp);
    } else {
      return sexp;
    }
  }

  public SEXP simplify(SEXP sexp) {
    if(sexp instanceof DeferredComputation &&
        ((DeferredComputation) sexp).getComputationDepth() > VectorPipeliner.MAX_DEPTH) {
      return session.getVectorEngine().simplify((DeferredComputation)sexp);
    } else {
      return sexp;
    }
  }
  
  public SEXP evaluate(SEXP expression, Environment rho) {
    if(expression instanceof Symbol) {
      return evaluateSymbol((Symbol)expression, rho);
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

  public <T> T getSingleton(Class<T> clazz) {
    return session.getSingleton(clazz);
  }

  public <T> void setState(T instance) {
    this.<T>setState((Class<T>) instance.getClass(), instance);
  }

  public <T> void setState(Class<T> clazz, T instance) {
    if(stateMap == null) {
      stateMap = Maps.newHashMap();
    }
    stateMap.put(clazz, instance);
  }

  private SEXP evaluateSymbol(Symbol symbol, Environment rho) {
    clearInvisibleFlag();

    if(symbol == Symbol.MISSING_ARG) {
      return symbol;
    }
    SEXP value = rho.findVariable(symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException(String.format("object '%s' not found", symbol.getPrintName()));
    } 
    
    if(value instanceof Promise) {
      return evaluate(value, rho);
    } else {
      return value;
    }
  }
  
  private SEXP evaluateExpressionVector(ExpressionVector expressionVector, Environment rho) {
    if(expressionVector.length() == 0) {
      setInvisibleFlag();
      return Null.INSTANCE;
    } else {
      SEXP result = Null.INSTANCE;
      int i=0;
      SEXP srcRefs = expressionVector.getAttribute(R_SrcrefSymbol);
      //srcRef = Null.INSTANCE;
      SEXP srcFile = expressionVector.getAttribute(R_SrcfileSymbol);
      for(SEXP sexp : expressionVector) {
        if (srcRefs!=Null.INSTANCE) {
           //srcRef = VECTOR_ELT(srcRefs, i);
        } 
        // DEBUG
        // if (context.inDebugMode)) {
        //    System.out.println("file:"+srcFile+", line:"+srcRef.asReal()+", exp:"+sexp);
        // }
        result = evaluate(sexp, rho);
        i++;
      }
      return result;
    }
  }

  private SEXP evaluateCall(FunctionCall call, Environment rho) {
    clearInvisibleFlag();
    SEXP srcRefs = call.getAttribute(R_SrcrefSymbol);
    SEXP srcFile = call.getAttribute(R_SrcfileSymbol);
    if (srcRefs != Null.INSTANCE) {
       //this.srcRef = VECTOR_ELT(srcRefs,0);
       //this.srcFile = srcFile;
    }
    //System.out.println("FunctionCall: file:"+srcFile+", line:"+srcRefs /*+", call:"+call */);
    Function functionExpr = evaluateFunction(call.getFunction(), rho);
    return functionExpr.apply(this, rho, call, call.getArguments());
  }

  private Function evaluateFunction(SEXP functionExp, Environment rho) {
    if(functionExp instanceof Symbol) {
      Symbol symbol = (Symbol) functionExp;
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
   * Translates a uri/path into a VFS {@code FileObject}.
   *
   * @param uri uniform resource indicator. This could be, for example:
   * <ul>
   * <li>jar:file:///path/to/my/libray.jar!/mylib/R/mylib.R</li>
   * <li>/usr/lib</li>
   * <li>c:&#92;users&#92;owner&#92;data.txt</li>
   * </ul>
   *
   * @return
   * @throws FileSystemException
   */
  public FileObject resolveFile(String uri) throws FileSystemException {
    return getFileSystemManager().resolveFile(session.getWorkingDirectory(), uri);
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

  public Closure getClosure() {
    return closure;
  }

  public PairList getArguments() {
    if(type != Type.FUNCTION) {
      throw new IllegalStateException("Only Contexts of type FUNCTION contain a FunctionCall");
    }
    return arguments;
  }

  public SEXP getFunctionName() {
    return call.getFunction();
  }

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
      if (cptr.getType() == Type.FUNCTION )
        nframe++;
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
   * @param function an expression that evaluates to a function that accepts a signaled
   * condition as an argument.
   */
  public void setConditionHandler(String conditionClass, SEXP function) {
    conditionHandlers.put(conditionClass, function);
  }

  public SEXP getConditionHandler(String conditionClass) {
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
    BaseFrame baseFrame = (BaseFrame) session.getBaseEnvironment().getFrame();
    baseFrame.load(this);
    
    evaluate(FunctionCall.newCall(Symbol.get(".onLoad")), session.getBaseNamespaceEnv());
    
//    evalBaseResource("/org/renjin/library/base/R/Rprofile");
//    
//    // FunctionCall.newCall(new Symbol(".OptRequireMethods")).evaluate(this, environment);
//    evaluate( FunctionCall.newCall(Symbol.get(".First.sys")), environment);
  }

  protected void evalBaseResource(String resourceName) throws IOException {
    Context evalContext = this.beginEvalContext(session.getBaseNamespaceEnv());
    InputStream in = getClass().getResourceAsStream(resourceName);
    if(in == null) {
      throw new IOException("Could not load resource '" + resourceName + "'");
    }
    Reader reader = new InputStreamReader(in);
    try {
      evalContext.evaluate(RParser.parseSource(reader, mkString(resourceName)));
    } finally {
      reader.close();
    }
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
    return getGlobalEnvironment().getBaseEnvironment();
  }

  public NamespaceRegistry getNamespaceRegistry() {
    return session.getNamespaceRegistry();
  }

  public SEXP getSrcRef()
  { return srcRef; }

  public void setSrcRef(SEXP srcRef)
  { this.srcRef = srcRef; }

  public SEXP getSrcFile()
  { return srcFile; }

  public void setSrcFile(SEXP srcFile)
  { this.srcFile = srcFile; }

}
