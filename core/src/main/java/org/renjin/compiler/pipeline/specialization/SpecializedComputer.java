package org.renjin.compiler.pipeline.specialization;

import org.renjin.sexp.Vector;

/**
 * A Just-in-time compiled computation for a deferred
 * computation graph of specific types
 */
public interface SpecializedComputer {

  /**
   *
   * @param operands the flattened set of vectors from a {@link org.renjin.compiler.pipeline.DeferredNode} and its descendants.
   * @return
   */
  public double[] compute(Vector[] operands);
}
