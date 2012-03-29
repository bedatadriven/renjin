package org.renjin.jvminterop;

import org.renjin.sexp.SEXP;

public interface MemberBinding {

  SEXP getValue(Object instance);
  
  void setValue(Object instance, SEXP value);
}
