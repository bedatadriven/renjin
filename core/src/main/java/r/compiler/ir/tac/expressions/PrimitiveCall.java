package r.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import r.base.Primitives;
import r.lang.Context;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.PrimitiveFunction;
import r.lang.SEXP;
import r.lang.StrictPrimitiveFunction;
import r.lang.Symbol;

/**
 * 
 */
public class PrimitiveCall implements Expression {

  private Symbol name;
  private final List<Expression> arguments;
  private PrimitiveFunction function;
  private SEXP[] argumentValues;
  
  public PrimitiveCall(Symbol name, List<Expression> arguments) {
    super();
    this.name = name;
    this.arguments = arguments;
    this.function = Primitives.getBuiltin(name);
    this.argumentValues = new SEXP[arguments.size()];
  }

  public PrimitiveCall(String name, Expression... arguments) {
    this(Symbol.get(name), Lists.newArrayList(arguments));
  }
  
  public Symbol getName() {
    return name;
  }
  
  public List<Expression> getArguments() {
    return arguments;
  }
  
  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    // build argument list 
    if(function instanceof StrictPrimitiveFunction) {
      for(int i=0;i!=argumentValues.length;++i) {
        argumentValues[i] = (SEXP)arguments.get(i).retrieveValue(context, temps);
      }
      return ((StrictPrimitiveFunction) function).applyStrict(context, context.getEnvironment(), argumentValues);
    } else {
      PairList.Builder argList = new PairList.Builder();
      for(Expression operand : arguments) {
        argList.add((SEXP)operand.retrieveValue(context, temps));
      }
   
      PairList args = argList.build();
      FunctionCall call = new FunctionCall(name, args);
      return function.apply(context, context.getEnvironment() , call, args);
    }
  }
  
  @Override
  public String toString() {
    String statement;
    if(name.getPrintName().equals(">") || name.getPrintName().equals("<")) {
      statement = "primitive< " + name + " >";
    } else {
      statement = "primitive<" + name + ">";
    }
    return statement + "(" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    for(Expression operand : arguments) {
      variables.addAll(operand.variables());
    }
    return Collections.unmodifiableSet(variables);
  }

  @Override
  public PrimitiveCall replaceVariable(Variable name, Variable newName) {
    List<Expression> newOps = Lists.newArrayListWithCapacity(arguments.size());
    for(Expression argument : arguments) {
      newOps.add(argument.replaceVariable(name, newName));
    }
    return new PrimitiveCall(this.name, newOps);
  }  
}
