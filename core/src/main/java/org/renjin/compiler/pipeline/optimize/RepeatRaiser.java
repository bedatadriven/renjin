package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.node.FunctionNode;

/**
 *
 */
public class RepeatRaiser implements Optimizer {
  @Override
  public boolean optimize(DeferredGraph graph, FunctionNode node) {
    return false;
  }
}
