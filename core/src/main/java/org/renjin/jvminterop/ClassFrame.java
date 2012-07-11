package org.renjin.jvminterop;

import java.util.Set;

import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Frame;
import org.renjin.sexp.Function;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;


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
    if(name == Symbols.CLASS) {
      return Environment.createChildEnvironment(Environment.EMPTY, new ObjectFrame(binding.getBoundClass()));
    }
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
