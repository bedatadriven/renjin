package r.jvmi.r2j;

import java.util.Set;

import r.lang.Frame;
import r.lang.Function;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class ClassFrame implements Frame {
  
  private final ClassBinding binding;
  
  public ClassFrame(ClassBinding binding) {
    this.binding = binding;
  }
  
  @Override
  public Set<Symbol> getSymbols() {
    return binding.getStaticMembers();
  }

  @Override
  public SEXP getVariable(Symbol name) {
    SEXP value = binding.getStaticMember(name);
    if(value == null) {
      return Symbol.UNBOUND_VALUE;
    }
    return value;
  }
  
  public Class getBoundClass() {
    return binding.getBoundClass();
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
    throw new EvalException("Cannot set values on JVM class frame");
  }

  @Override
  public void clear() {
    throw new EvalException("Cannot clear a JVM class frame");
  }

  @Override
  public void remove(Symbol name) {
    throw new EvalException("Cannot remove from a JVM class frame");  
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    return false;
  }
  
  
}
