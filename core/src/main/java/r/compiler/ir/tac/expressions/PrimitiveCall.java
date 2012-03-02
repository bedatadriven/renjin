package r.compiler.ir.tac.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import r.base.Primitives;
import r.base.special.ReturnException;
import r.compiler.ir.exception.InvalidSyntaxException;
import r.compiler.ir.tac.IRBody;
import r.compiler.ir.tac.IRBodyBuilder;
import r.jvmi.binding.JvmMethod;
import r.lang.Context;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.PrimitiveFunction;
import r.lang.Promise;
import r.lang.PromisePairList;
import r.lang.SEXP;
import r.lang.StrictPrimitiveFunction;
import r.lang.Symbol;
import r.lang.Symbols;

/**
 * 
 */
public class PrimitiveCall implements Expression {

  private Symbol name;
  
  /**
   * We need to retain the original FunctionCall, even for 
   * strict primitives, because we many primitives actually
   * require us to search for a "generic" implementation first,
   * which might be a closure. 
   */
  private FunctionCall call;
  
  private final List<Expression> arguments;
  private PrimitiveFunction function;
  private SEXP[] argumentValues;
  
  /**
   * Elipses (...) need to be handled specially because they are 
   * actually merged into the argument list
   */
  private boolean elipses;
  
  public PrimitiveCall(FunctionCall call, Symbol name, List<Expression> arguments) {
    super();
    this.call = call;
    this.name = name;
    this.arguments = arguments;
    
    for(Expression argument : arguments) {
      if(argument instanceof EnvironmentVariable && 
          ((EnvironmentVariable) argument).getName() == Symbols.ELLIPSES) {
        elipses = true;
        break;
      }
    }
    
    this.function = Primitives.getBuiltin(name);
    if(function == null) {
      function = Primitives.getInternal(name);
    }
    if(function == null) {
      throw new InvalidSyntaxException("No such primitive '" + function + "'");
    }
    this.argumentValues = new SEXP[arguments.size()];
  }
  
  public PrimitiveCall(FunctionCall call, String name, Expression... arguments) {
    this(call, Symbol.get(name), Lists.newArrayList(arguments));
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
    if(!elipses && function instanceof StrictPrimitiveFunction) {
      return applyStrict(context, temps);
    } else {
      return apply(context, temps);
    }
  }


  private Object apply(Context context, Object[] temps) {

    PairList.Builder argList = new PairList.Builder();
    int i = 0;
    for(PairList.Node node : call.getArguments().nodes()) {
      Expression expr = arguments.get(i++);
      if(expr instanceof EnvironmentVariable &&
          ((EnvironmentVariable) expr).getName() == Symbols.ELLIPSES) {
        argList.add(node.getRawTag(), Symbols.ELLIPSES);
      } else {
        SEXP evaled = (SEXP)expr.retrieveValue(context, temps);
        argList.add(node.getRawTag(), maybeRepromise(evaled));
      }
    }
  
    PairList args = argList.build();
    FunctionCall call = new FunctionCall(name, args);
    return function.apply(context, context.getEnvironment() , call, args);  
  }

  private SEXP maybeRepromise(SEXP value) {
    // the argument has already been evaluated;
    // if it is not constant, we need to package it in a promise
    // to assure that it does not get reevaluated
    if(IRBodyBuilder.isConstant(value)) {
      return value;
    } else {
      return new Promise(value, value);
    }
  }

  private Object applyStrict(Context context, Object[] temps) {
    for(int i=0;i!=argumentValues.length;++i) {
      SEXP value = (SEXP)arguments.get(i).retrieveValue(context, temps);
      if(value instanceof Promise) {
        argumentValues[i] = ((Promise) value).force();
      } else {
        argumentValues[i] = value;
      }
    }
    return ((StrictPrimitiveFunction) function).applyStrict(context, context.getEnvironment(), call, argumentValues);
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
    return new PrimitiveCall(call, this.name, newOps);
  }

  @Override
  public List<Expression> getChildren() {
    return arguments;
  }

  @Override
  public void setChild(int i, Expression expr) {
    arguments.set(i, expr);
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitPrimitiveCall(this);
  }  
}
