package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;

/**
 *
 */
public class RepeatRaiser implements Optimizer {
  @Override
  public boolean optimize(DeferredGraph graph, DeferredNode node) {
    return false;
  }
}
