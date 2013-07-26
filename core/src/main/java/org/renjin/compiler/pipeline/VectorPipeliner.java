package org.renjin.compiler.pipeline;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public interface VectorPipeliner {

  public static boolean DEBUG = "true".equals(System.getProperty("renjin.vp.debug"));
  public static int MAX_DEPTH = 25;

  /**
   * Calculate the result of the deferred computation and store in memory.
   * @param root the root of the calculation
   * @return
   */
  Vector materialize(DeferredComputation root);


  /**
   * Conducts any necessary simplification of the deferred computation graph
   * to keep the graph from growing too large
   */
  Vector simplify(DeferredComputation sexp);
}
