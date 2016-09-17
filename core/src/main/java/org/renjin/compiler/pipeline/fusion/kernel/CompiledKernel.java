package org.renjin.compiler.pipeline.fusion.kernel;

import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.sexp.Vector;

/**
 * A Just-in-time compiled computation for a deferred
 * computation graph of specific types
 */
public interface CompiledKernel {

  /**
   *
   * @param operands the flattened set of vectors from a {@link DeferredNode} and its descendants.
   * @return
   */
  public double[] compute(Vector[] operands);
}
