package org.renjin.invoke.reflection;

import org.renjin.sexp.SEXP;

/**
 * Interface to bindings between a class member (property, field, method, or constructor)
 * and
 */
public interface MemberBinding {

  SEXP getValue(Object instance);
  
  void setValue(Object instance, SEXP value);
}
