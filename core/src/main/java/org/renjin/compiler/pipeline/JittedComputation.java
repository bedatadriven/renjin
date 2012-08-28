package org.renjin.compiler.pipeline;

import org.renjin.sexp.Vector;

/**
 * A Just-in-time compiled computation for a deferred
 * computation graph of specific types
 */
public interface JittedComputation {

  /**
   *
   * @param operands the flattened set of vectors from a {@link DeferredNode} and its descendants.
   * @return
   */
  public double[] compute(Vector[] operands);
}
