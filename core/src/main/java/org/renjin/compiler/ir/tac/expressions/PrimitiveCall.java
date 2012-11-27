package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.compiler.runtime.CompiledRuntime;
import org.renjin.compiler.runtime.UnimplementedPrimitive;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Primitives;
import org.renjin.primitives.annotations.processor.WrapperGenerator2;
import org.renjin.sexp.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 
 */
public class PrimitiveCall implements CallExpression {

  private Symbol name;
  
  /**
   * We need to retain the original FunctionCall, even for 
   * strict primitives, because we many primitives actually
   * require us to search for a "generic" implementation first,
   * which might be a closure. 
   */
  private FunctionCall call;
  
  private final List<Expression> arguments;
  private Method primitiveMethod;
  private String[] argumentNames;
  
  /**
   * Elipses (...) need to be handled specially because they are 
   * actually merged into the argument list
   */
  private int elipsesIndex;
  
  public PrimitiveCall(FunctionCall call, Symbol name, List<Expression> arguments) {
    super();
    this.call = call;
    this.name = name;
    this.arguments = arguments;
    this.argumentNames = new String[arguments.size()];
    for(int i=0;i!=argumentNames.length;++i) {
      argumentNames[i] = Strings.emptyToNull(call.getArguments().getName(i));
    }
    
    this.elipsesIndex = -1;
    for(int i=0;i!=arguments.size();++i) {
      if(arguments.get(i) == Elipses.INSTANCE) {
        elipsesIndex = i;
        break;
      }
    }
    
    this.primitiveMethod = findMethod(name, arguments.size());
    
  }
  
  private Method findMethod(Symbol name, int arity) {
    PrimitiveFunction fn = Primitives.getBuiltin(name);
    if(fn == null) {
      fn = Primitives.getInternal(name);
    }
    Class wrapperClass = null;
    try {
      wrapperClass = Class.forName(WrapperGenerator2.toFullJavaName(fn.getName()));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    try {

      return wrapperClass.getMethod("doApply", new Class[] {
          Context.class,
          Environment.class,
          FunctionCall.class,
          String[].class,
          SEXP[].class
      });
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    }
    return null;
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
    if(primitiveMethod == null) {
      throw new UnsupportedOperationException("doApply() method for " + name + " not found");
    }

    try {
        return applyStatic(context, temps);
    } catch (InvocationTargetException e) {
      if(e.getCause() instanceof EvalException) {
        throw (EvalException)e.getCause();
      } else {
        throw new EvalException(e.getCause());
      }
    } catch (Exception e) {
      throw new EvalException(e);
    }
  }
  
  private SEXP applyStatic(Context context, Object[] temps) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    // names and argument count are determined at 
    // compile time
    String[] argumentNames = this.argumentNames;
    SEXP[] argumentValues = new SEXP[arguments.size()];
    for(int i=0;i!=arguments.size();++i) {
      if(arguments.get(i) != Elipses.INSTANCE) {
        argumentValues[i] = (SEXP)arguments.get(i).retrieveValue(context, temps);
      }
    }
      
    PairList extraArgs;
    if(elipsesIndex != -1) {
      extraArgs = (PairList) context.getEnvironment().getVariable(Symbols.ELLIPSES);
      argumentNames = CompiledRuntime.spliceArgNames(argumentNames, extraArgs, elipsesIndex);
      argumentValues = CompiledRuntime.spliceArgValues(context, argumentValues, extraArgs, elipsesIndex);
    }
    
    return (SEXP) primitiveMethod.invoke(null, context, 
        context.getEnvironment(), 
        call, argumentNames, argumentValues);
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

  @Override
  public FunctionCall getSExpression() {
    return call;
  }

  /**
   * 
   * @return true if this call has '...' in its arguments, meaning that
   * the number and names of arguments will be determined at runtime
   */
  public boolean hasElipses() {
    return elipsesIndex!=-1;
  }

  public int getElipsesIndex() {
    return elipsesIndex;
  }
  
  public List<String> getArgumentNames() {
    return Arrays.asList(argumentNames);
  }  
  
  public Class getWrapperClass() {
    if(primitiveMethod == null) {
      return UnimplementedPrimitive.class;
    }
    return primitiveMethod.getDeclaringClass();
  }
}
