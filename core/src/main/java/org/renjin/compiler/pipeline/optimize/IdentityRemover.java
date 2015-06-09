package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.vector.DeferredComputation;

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
    String op = ((DeferredComputation) node.getVector()).getComputationName();

    if("^".equals(op)) {
      if (node.getOperand(1).hasValue(1.0)) {
        return node.getOperand(0);
      } 
      // TODO: x^0
    } 
    
    if ("*".equals(op)) {        
      if (node.getOperand(0).hasValue(1.0)) {
        return node.getOperand(1);
      }
      if (node.getOperand(1).hasValue(1.0)) {
        return node.getOperand(0);
      }
      // TODO: x*0.0
    }
    if ("+".equals(op) || "-".equals(op)) {
      if (node.getOperand(0).hasValue(0.0)) {
        return node.getOperand(1);
      }
      if (node.getOperand(1).hasValue(0.0)) {
        return node.getOperand(0);
      }
    }
    return null;

  }
}
