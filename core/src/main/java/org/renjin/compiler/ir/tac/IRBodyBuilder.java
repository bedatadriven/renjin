package org.renjin.compiler.ir.tac;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.functions.FunctionCallTranslator;
import org.renjin.compiler.ir.tac.functions.FunctionCallTranslators;
import org.renjin.compiler.ir.tac.functions.TranslationContext;
import org.renjin.compiler.ir.tac.statements.*;
import org.renjin.eval.Context;
import org.renjin.invoke.reflection.MethodFunction;
import org.renjin.packaging.SerializedPromise;
import org.renjin.sexp.*;

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
  private int nextLocalVariableIndex = 0;
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

  public IRBodyBuilder(Context context, Environment rho) {
    this.context = context;
    this.rho = rho;
  }
  
  public IRBody build(SEXP exp) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();
    
    TranslationContext context = new TopLevelContext();
    Expression returnValue = translateExpression(context, exp);
    
    addStatement(new ReturnStatement(returnValue));
   
    removeRedundantJumps();
    
    return new IRBody(statements, labels, nextTemp);
  }
  
  public void dump(SEXP exp) {
    System.out.println( build(exp ).toString());
  }

  public Expression translateExpression(TranslationContext context, SEXP exp) {
    if(exp instanceof ExpressionVector) {
      return translateExpressionList(context, (ExpressionVector)exp);
    } else if(exp instanceof Symbol) {
      if(exp == Symbol.MISSING_ARG) {
        return SexpConstant.valueOf(exp);
      } else {
        return getEnvironmentVariable((Symbol) exp);
      }
    } else if(exp instanceof FunctionCall) {
      return translateCallExpression(context, (FunctionCall) exp);
    } else {
      // environments, pairlists, etc
      return SexpConstant.valueOf(exp);
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
      Function resolvedFunction = resolveFunctionSymbol((Symbol) functionName);

      if(resolvedFunction instanceof PrimitiveFunction) {
        return resolvedFunction;
      } else if(resolvedFunction instanceof MethodFunction) {
        return resolvedFunction;
      } else {
        throw new NotCompilableException(functionName, "Cannot compile reference to function '" + functionName + "', " +
            "resolves to function of class " + resolvedFunction.getClass().getName());
      }
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

  public List<Expression> translateArgumentList(TranslationContext context, PairList argumentSexps) {
    List<Expression> arguments = Lists.newArrayList();
    for(SEXP arg : argumentSexps.values()) {
      if(arg == Symbols.ELLIPSES) {
        throw new NotCompilableException(arg);
      } else {
        arguments.add( simplify( translateExpression(context, arg) ));
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
      return SexpConstant.valueOf(Null.INSTANCE);
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
  
  public LocalVariable newLocalVariable() {
    return new LocalVariable("Λ" + (nextLocalVariableIndex++));
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
    
  }
}
