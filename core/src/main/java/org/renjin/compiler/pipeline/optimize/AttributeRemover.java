package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.vector.AttributeDecoratingVector;

public class AttributeRemover implements Optimizer {

  public AttributeRemover() {
  }

  @Override
  public boolean optimize(DeferredGraph graph, DeferredNode node) {
    if(node.getVector() instanceof AttributeDecoratingVector) {
      graph.replaceNode(node, node.getOperand(0));
      return true;
    }
    return false;
  }
}
