package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.node.ComputationNode;

public interface Optimizer {
  
  boolean optimize(DeferredGraph graph, ComputationNode node);
}
