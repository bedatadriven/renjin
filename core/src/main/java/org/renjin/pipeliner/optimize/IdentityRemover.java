/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.pipeliner.optimize;

import org.renjin.pipeliner.DeferredGraph;
import org.renjin.pipeliner.node.DeferredNode;
import org.renjin.pipeliner.node.FunctionNode;
import org.renjin.primitives.vector.DeferredComputation;

/**
 * Remove mathematical identies like (x^1) or (x+1) or (+x)
 */
public class IdentityRemover implements Optimizer {
  private static boolean DEBUG = false;

  @Override
  public boolean optimize(DeferredGraph graph, FunctionNode node) {
    DeferredNode replacementValue = trySimplify(node);
    if(replacementValue != null) {
      graph.replaceNode(node, replacementValue);
      return true;
    }
    return false;
  }

  private DeferredNode trySimplify(FunctionNode node) {
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
