package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;

public interface Optimizer {
  boolean optimize(DeferredGraph graph, DeferredNode node);
}
