package org.renjin.invoke.reflection.converters;


import org.renjin.sexp.Vector;

public interface AtomicVectorConverter {

  Vector.Type getVectorType();
  
}
