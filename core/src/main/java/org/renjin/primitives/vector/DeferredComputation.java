package org.renjin.primitives.vector;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Vector;

/**
 * Marker interface for vectors whose computation has been deferred.
 * Deferred computations must be entirely pure: there can be no side
 * effects to their computation, and they must produce the same result
 * no matter how many times they are invoked.
 *
 * <p>
 *   Futhermore, a DeferredComputation of a given {@code class} must
 *   produce the same result given equal operands. That is, all details
 *   of the computation must be exposed as operands. If attributes play
 *   a role in computations, those attributes must be explicitly exposed
 *   as operands.
 * </p>
 */
public interface DeferredComputation extends AtomicVector {

  /**
   * @return this
   */
  Vector[] getOperands();

  /**
   * @return the name of the computation ("sum", "*", "/", etc), used
   * mostly for debugging.
   */
  String getComputationName();
}
