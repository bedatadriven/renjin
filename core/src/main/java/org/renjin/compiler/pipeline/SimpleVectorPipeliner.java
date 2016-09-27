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


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedDoubleVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

public class SimpleVectorPipeliner implements VectorPipeliner {
  @Override
  public Vector materialize(DeferredComputation root) {
    DeferredGraph graph = new DeferredGraph(root);

    if(VectorPipeliner.DEBUG) {
      System.err.println("materialize");
      graph.dumpGraph();
    }
    
    forceMemoizedValues(graph.getRoot());

    return graph.getRoot().getVector();
  }

  @Override
  public Vector simplify(DeferredComputation root) {
    DeferredGraph graph = new DeferredGraph(root);

    if(VectorPipeliner.DEBUG) {
      System.err.println("simplify");
      graph.dumpGraph();
    }

    Vector vector = materialize(root);
    if(vector instanceof MemoizedDoubleVector) {
      return vector;
    } else if(vector.isDeferred() && vector instanceof DoubleVector) {
      return DoubleArrayVector.unsafe(((DoubleVector) vector).toDoubleArray(), vector.getAttributes());
    } else {
      return vector;
    }
  }

  private void forceMemoizedValues(DeferredNode node) {
    for(DeferredNode child : node.getOperands()) {
      forceMemoizedValues(child);
    }
    if(node.isMemoized()) {
      new DeferredNodeComputer(node).run();
    }
  }
}
