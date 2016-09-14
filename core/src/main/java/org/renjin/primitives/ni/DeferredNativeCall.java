package org.renjin.primitives.ni;

public interface DeferredNativeCall {


  /**
   * 
   * @return true if the call has already been evaluated.
   */
  boolean isEvaluated();

  /**
   * @return the array for the given {@code outputIndex}
   */
  Object output(int outputIndex);

}
