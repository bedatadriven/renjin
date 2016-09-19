package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.node.FunctionNode;

public interface Optimizer {
  
  boolean optimize(DeferredGraph graph, FunctionNode node);
}
