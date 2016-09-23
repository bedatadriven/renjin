/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.pipeline;

import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Vector;


/**
 * Fully computes a node and stores its value
 */
public class DeferredNodeComputer implements Runnable {

  private final DeferredNode node;

  public DeferredNodeComputer(DeferredNode node) {
    this.node = node;
  }

  @Override
  public void run() {
    // TODO: at the moment, we can compile only a small number of summary
    // function, eventually we want to generate bytecode on the fly based
    // on their implementations elsewhere.
    if(node.getComputation().getComputationName().equals("mean") ||
        node.getComputation().getComputationName().equals("rowMeans") ||
        node.getComputation().getComputationName().equals("sum")) {
      try {
        Vector[] operands = node.flattenVectors();
        JittedComputation computer = DeferredJitCache.INSTANCE.compile(node);

        long start = System.nanoTime();

        Vector result = DoubleArrayVector.unsafe(computer.compute(operands));

        long time = System.nanoTime() - start;
        if(VectorPipeliner.DEBUG) {
          System.out.println("compute: " + (time/1e6) + "ms");
        }

        ((MemoizedComputation)node.getVector()).setResult(result);
        node.setResult(result);
      } catch(Throwable e) {
        throw new RuntimeException("Exception compiling node " + node, e);
      }
    } else if(node.getVector() instanceof MemoizedComputation) {
      node.setResult(((MemoizedComputation) node.getVector()).forceResult());
    }
  }
}
