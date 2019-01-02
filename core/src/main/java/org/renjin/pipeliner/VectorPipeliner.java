/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.pipeliner;

import org.renjin.eval.Profiler;
import org.renjin.pipeliner.fusion.LoopKernelCache;
import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedDoubleVector;
import org.renjin.repackaged.guava.util.concurrent.ListeningExecutorService;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.*;

import java.util.concurrent.ExecutorService;

public class VectorPipeliner {


  public static boolean DEBUG = "true".equals(System.getProperty("renjin.vp.debug"));
  public static int MAX_DEPTH = 25;
  
  private final ListeningExecutorService executorService;

  private final LoopKernelCache loopKernelCache;

  public VectorPipeliner(ExecutorService executorService) {
    this.executorService = MoreExecutors.listeningDecorator(executorService);
    this.loopKernelCache = new LoopKernelCache(executorService);
  }

  public void materialize(DeferredNativeCall call) {

    DeferredGraph graph = new DeferredGraph(call);
    graph.optimize(loopKernelCache);
    graph.dumpGraph();
    throw new UnsupportedOperationException("TODO");
  }

  public Vector materialize(Vector root) {

    DeferredGraph graph = new DeferredGraph(root);

    materializeGraph(graph);

    return graph.getRootResult(0);
  }

  public ListVector materialize(ListVector listVector) {

    DeferredGraph graph = new DeferredGraph();

    // Identify which elements of the list need to be materialized
    Vector[] vectors = new Vector[listVector.length()];
    for (int i = 0; i < listVector.length(); i++) {
      SEXP element = listVector.getElementAsSEXP(i);
      if(element instanceof Vector && ((Vector) element).isDeferred()) {
        Vector vector = (Vector) element;
        vectors[i] = vector;
        graph.addRoot(vector);
      }
    }

    materializeGraph(graph);

    // Reassemble a new list
    ListVector.Builder newList = new ListVector.Builder(0, listVector.length());
    newList.copyAttributesFrom(listVector);

    int vectorIndex = 0;
    for (int i = 0; i < listVector.length(); i++) {
      if(vectors[i] == null) {
        newList.add(listVector.getElementAsSEXP(i));
      } else {
        newList.add(graph.getRootResult(vectorIndex));
        vectorIndex++;
      }
    }

    return newList.build();

  }


  private void materializeGraph(DeferredGraph graph) {

    long start = System.nanoTime();

    if(VectorPipeliner.DEBUG) {
      System.out.print("unopt");
      graph.dumpGraph();
    }

    graph.optimize(loopKernelCache);

    if(VectorPipeliner.DEBUG) {
      graph.dumpGraph();
    }

    // force any memoized values in the graph
    evaluate(graph);

    if(Profiler.ENABLED) {
      long time = System.nanoTime() - start;
      Profiler.materialized(time);
    }
  }


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
  
  public void evaluate(DeferredGraph graph)  {
    DeferredGraphEval eval = new DeferredGraphEval(graph, executorService);
    eval.execute();
  }

}
