package org.renjin.invoke.reflection;

import org.renjin.sexp.SEXP;

public class StaticBinding implements MemberBinding {

  private MemberBinding binding;

  public StaticBinding(MemberBinding binding) {
    this.binding = binding;
  }

  @Override
  public SEXP getValue(Object instance) {
    return getValue();
  }

  public SEXP getValue() {
    return binding.getValue(null);
  }

  @Override
  public void setValue(Object instance, SEXP value) {
    setValue(null, value);
  }
}
