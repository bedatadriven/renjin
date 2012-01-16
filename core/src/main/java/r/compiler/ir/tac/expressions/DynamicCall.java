package r.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import r.lang.Context;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Function call that is invoked with the full R
 * flexibility, no assumptions are made...
 */
public class DynamicCall implements Expression {

  private Variable function;
  private final List<Expression> arguments;
  private final List<SEXP> argumentNames;
  
  public DynamicCall(Variable name, List<SEXP> argumentNames, List<Expression> arguments) {
    this.function = name;
    this.arguments = arguments;
    this.argumentNames = argumentNames;
  }

  public Variable getName() {
    return function;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    
    // locate function object
    EnvironmentVariable functionName = (EnvironmentVariable)function;
    Function function = findFunction(functionName.getName(), context);
    
    // build argument list 
    PairList.Builder argList = new PairList.Builder();
    for(int i=0;i!=arguments.size();++i) {
      argList.add(argumentNames.get(i), (SEXP)arguments.get(i).retrieveValue(context, temps));
    }
    PairList args = argList.build();
    FunctionCall call = new FunctionCall(functionName.getName(), args);
    
    return function.apply(context, context.getEnvironment(), call, args);
        
  }
  
  private Function findFunction(SEXP functionExp, Context context) {
    if(functionExp instanceof Symbol) {
      Symbol symbol = (Symbol) functionExp;
      Function fn = context.getEnvironment().findFunction(symbol);
      if(fn == null) {
        throw new EvalException("could not find function '%s'", symbol.getPrintName());      
      }
      return fn;
    } else {
      throw new UnsupportedOperationException("only symbols are supported in function calls right now");
    }
  }


  @Override
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    variables.addAll( function.variables() );
    for(Expression operand : arguments) {
      variables.addAll( operand.variables() );
    }
    return Collections.unmodifiableSet(variables);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("\u0394 " + function + "(");
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
    return new DynamicCall(
        (Variable)this.function.replaceVariable(name, newName), 
        argumentNames,
        newOps);
  }

  @Override
  public List<Expression> getChildren() {
    List<Expression> children = Lists.newArrayList();
    children.add(function);
    children.addAll(arguments);
    return children;
  }

  @Override
  public void setChild(int i, Expression expr) {
    if(i == 0) {
      function = (Variable)expr;
    } else {
      arguments.set(i-1, expr);
    }
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitDynamicCall(this);
  } 
}
