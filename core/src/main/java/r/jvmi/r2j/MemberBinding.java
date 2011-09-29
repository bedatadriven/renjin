package r.jvmi.r2j;

import r.lang.SEXP;

public interface MemberBinding {

  SEXP getValue(Object instance);
  
  void setValue(Object instance, SEXP value);
}
