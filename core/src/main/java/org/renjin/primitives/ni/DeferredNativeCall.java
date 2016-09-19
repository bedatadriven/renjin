package org.renjin.primitives.ni;

import org.renjin.sexp.Vector;

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

  String getDebugName();

  Vector[] getOperands();
  
  String getOutputName(int outputIndex);

  void evaluate(Vector[] operands);
}
