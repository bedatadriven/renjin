package r.jvmi.r2j;

import java.util.Set;

import r.lang.Frame;
import r.lang.Function;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

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
  public Function getFunction(Symbol name) {
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
  
  public Object getInstance() {
    return instance;
  }
}
