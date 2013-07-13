package org.renjin.compiler.pipeline;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.Vector;

public interface VectorPipeliner {
  boolean DEBUG = false;

  Vector materialize(DeferredComputation root);
}
