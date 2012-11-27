package org.renjin.jvminterop;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Frame;
import org.renjin.sexp.Function;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Set;


public class ObjectFrame implements Frame {

  private final ClassBinding classBinding;
  private final Object instance;
  
  public ObjectFrame(Object instance) {
    this.instance = instance;
    this.classBinding = ClassBinding.get(instance.getClass());
  }
  
  @Override
  public Set<Symbol> getSymbols() {
    return classBinding.getMembers();
  }

  @Override
  public SEXP getVariable(Symbol name) {
    MemberBinding binding = classBinding.getMemberBinding(name);
    if(binding == null) {
      return Symbol.UNBOUND_VALUE;
    }
    return binding.getValue(instance);
  }

  @Override
  public Function getFunction(Context context, Symbol name) {
    SEXP value = getVariable(name);
    if(value instanceof Function) {
      return (Function)value;
    } else {
      return null;
    }
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    MemberBinding binding = classBinding.getMemberBinding(name);
    if(binding == null) {
      throw new EvalException("The class '%s' has no property named '%s'", instance.getClass().getName(),
          name.getPrintName());
    }
    binding.setValue(instance, value);  
  }

  @Override
  public void clear() {
    throw new EvalException("Cannot clear a JVM object frame");
  }  
  
  
  
  @Override
  public void remove(Symbol name) {
    throw new EvalException("Cannot remove from JVM object frame");
  }

  public Object getInstance() {
    return instance;
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    return false;
  }
}
