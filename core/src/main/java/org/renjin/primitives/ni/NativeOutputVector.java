package org.renjin.primitives.ni;


import org.renjin.sexp.AtomicVector;

public interface NativeOutputVector extends AtomicVector {
  
  DeferredNativeCall getCall();
  
  int getOutputIndex();
}
