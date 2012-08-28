package org.renjin.primitives.vector;

import org.renjin.sexp.Vector;

/**
 * Marker interface for DeferredComputation whose value is <em>memoized</em>,
 * that is, it is not computed until it's result is used, but once computed
 * its result is stored internally to avoid being computed again.
 *
 */
public interface MemoizedComputation extends DeferredComputation {

  /**
   *
   * @return ensure that the result has been calculated and return it.
   */
  Vector forceResult();

  /**
   * Sets the results of the computation. This may be called in the event
   *  that the computation is done externally.
   */
  void setResult(Vector result);

  /**
   *
   * @return  true if the result has been calculated and is available in memory.
   */
  boolean isCalculated();

}
