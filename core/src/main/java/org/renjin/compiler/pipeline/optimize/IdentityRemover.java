package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.node.ComputationNode;
import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.primitives.vector.DeferredComputation;

/**
 * Remove mathematical identies like (x^1) or (x+1) or (+x)
 */
public class IdentityRemover implements Optimizer {
  private static boolean DEBUG = false;

  @Override
  public boolean optimize(DeferredGraph graph, ComputationNode node) {
    DeferredNode replacementValue = trySimplify(node);
    if(replacementValue != null) {
      graph.replaceNode(node, replacementValue);
      return true;
    }
    return false;
  }

  private DeferredNode trySimplify(ComputationNode node) {
    String op = node.getComputationName();


    if(node.getOperands().size() == 2) {

      if ("^".equals(op)) {
        if (node.getOperand(1).hasValue(1.0)) {
          if (DEBUG) {
            System.out.println("Killed ^1");
          }
          return node.getOperand(0);
        }
        // TODO: x^0, 0^x
      }

      if ("*".equals(op)) {
        if (node.getOperand(0).hasValue(1.0)) {
          if (DEBUG) {
            System.out.println("Killed 1*x");
          }
          return node.getOperand(1);

        }
        if (node.getOperand(1).hasValue(1.0)) {
          if (DEBUG) {
            System.out.println("Killed x*1");
          }
          return node.getOperand(0);
        }
        if (node.getOperand(0).hasValue(0.0) || node.getOperand(1).hasValue(0.0)) {
          // TODO if (DEBUG) System.out.println("Killed x*0, 0*x");
        }
      }
      if ("+".equals(op) || "-".equals(op)) {
        if (node.getOperand(0).hasValue(0.0)) {
          if (DEBUG) {
            System.out.println("Killed 0+-x");
          }
          return node.getOperand(1);
        }
        if (node.getOperand(1).hasValue(0.0)) {
          if (DEBUG) {
            System.out.println("Killed x+-0");
          }
          return node.getOperand(0);
        }
      }
    }

    if ("mean".equals(op) || "min".equals(op) || "max".equals(op)) {
      if (node.getOperand(0) instanceof DeferredComputation &&
          ((DeferredComputation) node.getOperand(0).getVector()).getComputationName().equals("rep")) {
        if (DEBUG) {
          System.out.println("Killed mean/max/min(rep(x))");
        }
        return node.getOperand(0).getOperand(0);
      }
    }
    // TODO: sum(rep(100, 2)) == 100*2
    return null;
  }
}
