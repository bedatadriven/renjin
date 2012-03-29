package org.renjin.jvminterop;

import java.lang.reflect.Method;

import org.renjin.eval.EvalException;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


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
