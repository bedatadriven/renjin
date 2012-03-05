package r.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import r.lang.BuiltinFunction;
import r.lang.Context;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Function call that is invoked with the full R
 * flexibility, no assumptions are made...
 */
public class DynamicCall implements Expression {


  /**
   * The S expression from the original AST which we 
   * need to pass to closures at runtime. For example, given the AST
   * <code>
   * x$f(33)
   * </code>
   * 
   * If x$f evaluates to a closure, a new Context needs to be created
   * that contains a copy of the original {@link FunctionCall}. So we 
   * need to retain the x$f literal.
   */
  private SEXP functionName;
  
  
  /**
   * The IR expression from which we will obtain the actual
   * function value at runtime, IF {@code functionName} is NOT a {@link Symbol}
   */
  private Expression functionExpr;
  
  /**
   * The original function call. We just need to pass this around.
   */
  private FunctionCall call;
  
  private final List<Expression> arguments;
  private final List<SEXP> argumentNames;
  private final String[] argumentNamesArray;
  private boolean elipses;
  
  public DynamicCall(FunctionCall call, Expression function, 
      List<SEXP> argumentNames, List<Expression> arguments) {
    this.call = call;
    this.functionName = call.getFunction();
    this.functionExpr = function;
    this.arguments = arguments;
    this.argumentNames = argumentNames;
    this.argumentNamesArray = new String[argumentNames.size()];
    for(int i=0;i!=argumentNames.size();++i) {
      if(argumentNames.get(i) instanceof Symbol) {
        argumentNamesArray[i] = ((Symbol) argumentNames.get(i)).getPrintName();
      }
    }
    this.elipses = false;
    for(Expression expression: arguments) {
      if(expression == Elipses.INSTANCE ) {
        elipses = true;
      }
    }
  }

  public Expression getFunction() {
    return functionExpr;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    
    // locate function object
    Function functionValue = findFunction(context, temps);
    
    if(! elipses && functionValue instanceof BuiltinFunction) {
      return ((BuiltinFunction)functionValue).apply(context, 
          context.getEnvironment(), call, argumentNamesArray, evaluateArgs(context, temps));
    } else {
      
      PairList.Builder args = new PairList.Builder();
      for(int i=0;i!=arguments.size();++i) {
        args.add(argumentNames.get(i), arguments.get(i).getSExpression());
      }
      
      return functionValue.apply(context, context.getEnvironment(), call, args.build());
    }
    
    // build argument list 
//    PairList.Builder argList = new PairList.Builder();
//    for(int i=0;i!=arguments.size();++i) {
//      argList.add(argumentNames.get(i), (SEXP)arguments.get(i).retrieveValue(context, temps));
//    }
//    PairList args = argList.build();
//    FunctionCall call = new FunctionCall(functionName, args);
//    
//    return functionValue.apply(context, context.getEnvironment(), call, args);
//  
  }
  

  private SEXP[] evaluateArgs(Context context, Object[] temps) {
    SEXP[] evaluated = new SEXP[arguments.size()];
    for(int i=0;i!=evaluated.length;++i) {
      evaluated[i] = (SEXP) arguments.get(i).retrieveValue(context, temps);
    }
    return evaluated;
  }

  private Function findFunction(Context context, Object[] temps) {
 
    // we have to different cases here. 
    // if the function call is in the form 
    //
    // f(a,b,c)
    // 
    // then we do a special lookup to find the first 
    // _function_ with the name `f` in the enclosing environments.
    // bindings with non-function values are ignored.
    //
    // this is *different* then simply evaluating the symbol `f`.
    
    if(functionName instanceof Symbol) {
      Symbol symbol = (Symbol) functionName;
      Function fn = context.getEnvironment().findFunction(symbol);
      if(fn == null) {
        throw new EvalException("could not find function '%s'", symbol.getPrintName());      
      }
      return fn;
    } else {
      
      // otherwise, we need to proceed to evaluate the expression 
      // as it's been translated into IR. It must evaluate to a
      // function value.
      
      Object value = functionExpr.retrieveValue(context, temps);
      if(!(value instanceof Function)) {
        throw new EvalException("attempt to apply non-function: " + value);
      }
      return (Function) value;
    }
  }


  @Override
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    variables.addAll( functionExpr.variables() );
    for(Expression operand : arguments) {
      variables.addAll( operand.variables() );
    }
    return Collections.unmodifiableSet(variables);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("\u0394 " + functionExpr + "(");
    for(int i=0;i!=argumentNames.size();++i) {
      if(i > 0) {
        sb.append(", ");
      }
      if(argumentNames.get(i) != Null.INSTANCE) {
        sb.append(argumentNames.get(i)).append(" = ");
      }
      sb.append(arguments.get(i));
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public Expression replaceVariable(Variable name, Variable newName) {
    List<Expression> newOps = Lists.newArrayListWithCapacity(arguments.size());
    for(Expression argument : arguments) {
      newOps.add(argument.replaceVariable(name, newName));
    }
    return new DynamicCall(call,
        (Variable)this.functionExpr.replaceVariable(name, newName), 
        argumentNames,
        newOps);
  }

  @Override
  public List<Expression> getChildren() {
    List<Expression> children = Lists.newArrayList();
    children.add(functionExpr);
    children.addAll(arguments);
    return children;
  }

  @Override
  public void setChild(int i, Expression expr) {
    if(i == 0) {
      functionExpr = (Variable)expr;
    } else {
      arguments.set(i-1, expr);
    }
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitDynamicCall(this);
  }

  @Override
  public SEXP getSExpression() {
    return call;
  } 
  
}
