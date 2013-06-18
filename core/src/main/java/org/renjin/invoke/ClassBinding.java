package org.renjin.invoke;


import org.renjin.invoke.reflection.MemberBinding;
import org.renjin.sexp.Symbol;

public interface ClassBinding {
  MemberBinding getMemberBinding(Symbol name);
}
