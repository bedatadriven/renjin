package r.jvmi.r2j;

import java.lang.reflect.Method;

import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class MethodBinding implements MemberBinding {
  
  private Symbol name;
  private FunctionBinding binding;
  
  public MethodBinding(Symbol name, Iterable<Method> overloads) {
    this.name = name;
    this.binding = new FunctionBinding(overloads);
  }

  @Override
  public SEXP getValue(Object instance) {
    return new MethodFunction(instance, binding);
  }

  @Override
  public void setValue(Object instance, SEXP value) {
    throw new EvalException("Cannot replace a method on an instance of a JVM object");
  }
}
