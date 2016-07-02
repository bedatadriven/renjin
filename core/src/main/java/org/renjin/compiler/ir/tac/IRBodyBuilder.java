package org.renjin.compiler.ir.tac;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.functions.*;
import org.renjin.compiler.ir.tac.statements.*;
import org.renjin.eval.Context;
import org.renjin.packaging.SerializedPromise;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Attempts to create an intermediate representation of the R code, partially
 * evaluating as it goes.
 *
 * The idea is that we are ONLY interested in the result if the R code can be
 * reduced to a reasonably static form that we can reason about. If there are
 * calls to eval(), assign() or other black holes, we abort and defer to the
 * AST interpreter.
 */
public class IRBodyBuilder {
  
  private int nextTemp = 0;
  private int nextLabel = 0;
  
  private FunctionCallTranslators builders = new FunctionCallTranslators();
 
  private List<Statement> statements;
  private IRLabel currentLabel;
  private Map<IRLabel, Integer> labels;
  private Map<Symbol, EnvironmentVariable> variables = Maps.newHashMap();

  private Context context;
  private Environment rho;

  /**
   * List of symbols that we have resolved to builtins / or inlined
   * closures. We need to check at the end that there is no possiblity
   * they have been assigned to.
   */
  private Set<Symbol> resolvedFunctions = Sets.newHashSet();
  
  private Map<String, Integer> localVariableNames = Maps.newHashMap();

  public IRBodyBuilder(Context context, Environment rho) {
    assert context != null;
    this.context = context;
    this.rho = rho;
  }
  
  public Context getEvaluationContext() {
    return context;
  }
  
  public IRBody build(SEXP exp) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();
    
    TranslationContext context = new TopLevelContext();
    Expression returnValue = translateExpression(context, exp);
    
    addStatement(new ReturnStatement(returnValue));
   
    removeRedundantJumps();
    insertVariableInitializations();
    updateVariableReturn();
    
    return new IRBody(statements, labels);
  }
  
  public IRBody buildLoopBody(FunctionCall call, SEXP sequence) {
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();

    LocalVariable vector = newLocalVariable("elements");
    LocalVariable counter = newLocalVariable("i");

    statements.add(new Assignment(vector, new ReadLoopVector(sequence)));
    statements.add(new Assignment(counter, new ReadLoopIt()));

    LoopBodyContext bodyContext = new LoopBodyContext(rho);
    
    ForTranslator.buildLoop(bodyContext, this, call, vector, counter);

    addStatement(new ReturnStatement(new Constant(Null.INSTANCE)));
    
    removeRedundantJumps();
    insertVariableInitializations();
    updateVariableReturn();
    
    return new IRBody(statements, labels);
  }
  
  public IRBody buildFunctionBody(Closure closure, Set<Symbol> suppliedArguments) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();
    
    // First define the parameters which will be supplied
    List<ReadParam> params = Lists.newArrayList();
    for (PairList.Node formal : closure.getFormals().nodes()) {
      if (suppliedArguments.contains(formal.getTag())) {
        ReadParam paramExpr = new ReadParam(formal.getTag());
        statements.add(new Assignment(new EnvironmentVariable(formal.getTag()), paramExpr));
        params.add(paramExpr);
      }
    }
    
    // Now define  default values for formals that are not supplied
    // These are not necessarily constants and are evaluated lazily, so some care is 
    // required. 
    for (PairList.Node formal : closure.getFormals().nodes()) {
      if (!suppliedArguments.contains(formal.getTag())) {
        SEXP defaultValue = formal.getValue();
        if (defaultValue != Symbol.MISSING_ARG) {
          if (!isConstant(defaultValue)) {
            throw new NotCompilableException(defaultValue, "Non-constant default value for argument " + formal.getName());
          }
          statements.add(new Assignment(new EnvironmentVariable(formal.getTag()), new Constant(formal.getValue())));
        }
      }
    }
    
    TranslationContext context = new InlinedContext();
    Expression returnValue = translateExpression(context, closure.getBody());
    addStatement(new ReturnStatement(returnValue));

    removeRedundantJumps();
    insertVariableInitializations();

    IRBody body = new IRBody(statements, labels);
    body.setParams(params);
    return body;
  }

  private boolean isConstant(SEXP defaultValue) {
    return ! ( defaultValue instanceof Symbol || 
               defaultValue instanceof ExpressionVector ||
               defaultValue instanceof FunctionCall);
    
  }

  private void updateVariableReturn() {

    for (Statement statement : statements) {
      if(statement instanceof ReturnStatement) {
        ((ReturnStatement) statement).addEnvironmentVariables(variables.values());
      }
    }
  }


  private void insertVariableInitializations() {
    // For every variable that comes from the environment, 
    // declare it as a constant in the beginning of the block
    
    List<Assignment> initializations = new ArrayList<>();
    
    for (EnvironmentVariable environmentVariable : variables.values()) {
      SEXP value = rho.findVariable(environmentVariable.getName());
      if(value instanceof Promise) {
        Promise promisedValue = (Promise) value;
        if(promisedValue.isEvaluated()) {
          value = promisedValue.force(context);
        } else {
          // Promises can have side effects, and evaluation order is important 
          // so we can't just force all the promises in the beginning of the loop
          throw new NotCompilableException(environmentVariable.getName(), "Unevaluated promise encountered");
        }
      }
      if(value != Symbol.UNBOUND_VALUE) {
        initializations.add(new Assignment(environmentVariable, new ReadEnvironment(environmentVariable.getName(), ValueBounds.of(value))));
      }
    }
    
    // Update the labels to reflect the additional statements at the beginning
    statements.addAll(0, initializations);
    for (IRLabel label : labels.keySet()) {
      labels.put(label, labels.get(label) + initializations.size());
    }
    
  }

  public void dump(SEXP exp) {
    System.out.println( build(exp ).toString());
  }

  public Expression translateExpression(TranslationContext context, SEXP exp) {
    if(exp instanceof ExpressionVector) {
      return translateExpressionList(context, (ExpressionVector)exp);
    } else if(exp instanceof Symbol) {
      if(exp == Symbol.MISSING_ARG) {
        return new Constant(exp);
      } else {
        return getEnvironmentVariable((Symbol) exp);
      }
    } else if(exp instanceof FunctionCall) {
      return translateCallExpression(context, (FunctionCall) exp);
    } else {
      // environments, pairlists, etc
      return new Constant(exp);
    }
  }

  public EnvironmentVariable getEnvironmentVariable(Symbol name) {
    EnvironmentVariable var = variables.get(name);
    if(var == null) {
      var = new EnvironmentVariable(name);
      variables.put(name, var);
    }
    return var;
  }

  public void translateStatements(TranslationContext context, SEXP sexp) {
    if(sexp instanceof FunctionCall) {
      FunctionCall call = (FunctionCall)sexp;
      Function function = resolveFunction(call.getFunction());
      builders.get( function ).addStatement(this, context, function, call);
    } else {
      Expression expr = translateExpression(context, sexp);
      if(!(expr instanceof Constant)) {
        addStatement(new ExprStatement(expr));
      }
    }
  }

  public Expression translateSetterCall(TranslationContext context, FunctionCall getterCall, Expression rhs) {
    Symbol getter = (Symbol) getterCall.getFunction();
    Function setter = resolveFunction(Symbol.get(getter.getPrintName() + "<-"));

    FunctionCallTranslator translator = builders.get(setter);
    return translator.translateToSetterExpression(this, context, setter, getterCall, rhs);
  }

  public Expression translateCallExpression(TranslationContext context, FunctionCall call) {
    SEXP functionName = call.getFunction();
    Function function = resolveFunction(functionName);

    FunctionCallTranslator translator = builders.get(function);
    return translator.translateToExpression(this, context, function, call);
  }

  private Function resolveFunction(SEXP functionName) {
    if( functionName instanceof PrimitiveFunction) {
      return (PrimitiveFunction) functionName;
    } else if (functionName instanceof Symbol) {
      return resolveFunctionSymbol((Symbol) functionName);
    }
    throw new NotCompilableException(functionName);
  }

  private Function resolveFunctionSymbol(Symbol functionName) {
    Environment environment = rho;
    while(environment != Environment.EMPTY) {
      Function f = isFunction(functionName, environment.getVariable(functionName));
      if(f != null) {
        return f;
      }
      environment = environment.getParent();
    }
    throw new NotCompilableException(functionName, "Could not find function " + functionName);
  }

  /**
   * Tries to safely determine whether the expression is a function, without
   * forcing any promises that might have side effects.
   * @param exp
   * @return null if the expr is definitely not a function, or {@code expr} if the
   * value can be resolved to a Function without side effects
   * @throws org.renjin.compiler.NotCompilableException if it is not possible to determine
   * whether the value is a function without risking side effects.
   */
  private Function isFunction(Symbol functionName, SEXP exp) {
    if(exp instanceof Function) {
      return (Function)exp;

    } else if(exp instanceof SerializedPromise) {
      return isFunction(functionName, exp.force(this.context));

    } else if(exp instanceof Promise) {
      Promise promise = (Promise)exp;
      if(promise.isEvaluated()) {
        return isFunction(functionName, promise.getValue());
      } else {
        throw new NotCompilableException(functionName, "Symbol " + functionName + " cannot be resolved to a function " +
            " an enclosing environment has a binding of the same name to an unevaluated promise");
      }
    } else {
      return null;
    }
  }

  public List<IRArgument> translateArgumentList(TranslationContext context, PairList argumentSexps) {
    List<IRArgument> arguments = Lists.newArrayList();
    for(PairList.Node argNode : argumentSexps.nodes()) {
      if(argNode.getValue() == Symbols.ELLIPSES) {
        for (PairList.Node extraArgument :  context.getEllipsesArguments().nodes()) {
          SimpleExpression expression = simplify(translateExpression(context, extraArgument));
          arguments.add( new IRArgument(extraArgument.getRawTag(), expression) );
        }
      } else {
        SimpleExpression argExpression = simplify(translateExpression(context, argNode.getValue()));
        arguments.add( new IRArgument(argNode.getRawTag(), argExpression) );
      }
    }
    return arguments;
  }

  public SimpleExpression simplify(Expression rvalue) {
    if(rvalue instanceof SimpleExpression) {
      return (SimpleExpression) rvalue;
    } else {
      Temp temp = newTemp();
      addStatement(new Assignment(temp, rvalue));
      return temp;      
    }
  }

  public SimpleExpression translateSimpleExpression(TranslationContext context, SEXP exp) {
    return simplify(translateExpression(context, exp));
  }
  
  private Expression translateExpressionList(TranslationContext context, ExpressionVector vector) {
    if(vector.length() == 0) {
      return new Constant(Null.INSTANCE);
    } else {
      for(int i=0;i+1<vector.length();++i) {
        translateStatements(context, vector.getElementAsSEXP(i));
      }
      return translateExpression(context, vector.getElementAsSEXP(vector.length()-1));
    }
  }
  
  public Temp newTemp() {
    return new Temp(nextTemp++);
  }
  
  public LocalVariable newLocalVariable(String debugName) {
    int index;
    String name;
    if(localVariableNames.containsKey(debugName)) {
      index = localVariableNames.get(debugName) + 1;
      name = debugName + index;
    } else {
      index = 1;
      name = debugName;
    }
    localVariableNames.put(debugName, index);
    return new LocalVariable("_" + name);
  }
  
  public IRLabel newLabel() {
    return new IRLabel(nextLabel++);
  }

  public void addStatement(Statement statement) {
    statements.add(statement);
    currentLabel = null;
  }
  
  public IRLabel addLabel() {
    if(currentLabel != null) {
      return currentLabel; 
    } else {
      IRLabel newLabel = newLabel();
      addLabel(newLabel);
      return newLabel;
    }
  }

  public void addLabel(IRLabel label) {
    labels.put(label, statements.size());
    currentLabel = label;
  }
  
  /**
   * Streamlines IR in the case that you have one goto 
   * pointing to another goto.
   */
  private void removeRedundantJumps() {
    boolean changed;
    do {
      changed = false;
      for(int i=0;i!=statements.size();++i) {
        Statement stmt = statements.get(i);
        if(stmt instanceof IfStatement) {
          IfStatement ifStmt = (IfStatement) stmt;
        
          IRLabel newTrueTarget = ultimateTarget(ifStmt.getTrueTarget());
          if(newTrueTarget != null) {
            statements.set(i, ifStmt.setTrueTarget(newTrueTarget));
            changed = true;
          }
          
          IRLabel newFalseTarget = ultimateTarget(ifStmt.getFalseTarget());
          if(newFalseTarget != null) {
            statements.set(i, ifStmt.setFalseTarget(newFalseTarget));
            changed = true;
          }
        }
      }
    } while(changed);
  }
  
  private IRLabel ultimateTarget(IRLabel label) {
    Statement targetStmt = statements.get( labels.get(label) );
    if(targetStmt instanceof GotoStatement) {
      return ((GotoStatement) targetStmt).getTarget();
    }
    return null;
  }
  
  private static class TopLevelContext implements TranslationContext {

    @Override
    public PairList getEllipsesArguments() {
      throw new InvalidSyntaxException("'...' used outside of a function");
    }
  }
}
