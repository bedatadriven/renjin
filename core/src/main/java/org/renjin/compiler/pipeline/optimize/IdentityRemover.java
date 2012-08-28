package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;

/**
 * Remove mathematical identies like (x^1) or (x+1) or (+x)
 */
public class IdentityRemover implements Optimizer {

  @Override
  public boolean optimize(DeferredGraph graph, DeferredNode node) {
    if(node.isComputation()) {
      DeferredNode replacementValue = trySimplify(node);
      if(replacementValue != null) {
        graph.replaceNode(node, replacementValue);
        return true;
      }
    }
    return false;
  }

  private DeferredNode trySimplify(DeferredNode node) {
    if(node.getComputation().getComputationName().equals("^") &&
            node.getOperand(1).hasValue(1.0)) {
      return node.getOperand(0);
    } else {
      return null;
    }

  }
}
