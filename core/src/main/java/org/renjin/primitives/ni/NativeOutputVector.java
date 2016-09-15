package org.renjin.primitives.ni;


public interface NativeOutputVector {
  
  DeferredNativeCall getCall();
  
  int getOutputIndex();
}
