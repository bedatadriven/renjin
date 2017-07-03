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

import org.renjin.eval.Profiler;
import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedDoubleVector;
import org.renjin.repackaged.guava.util.concurrent.ListeningExecutorService;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

import java.util.concurrent.ExecutorService;

public class VectorPipeliner {


  public static boolean DEBUG = "true".equals(System.getProperty("renjin.vp.debug"));
  public static int MAX_DEPTH = 25;
  
  private final ListeningExecutorService executorService;

  public VectorPipeliner(ExecutorService executorService) {
    this.executorService = MoreExecutors.listeningDecorator(executorService);
  }

  public void materialize(DeferredNativeCall call) {

    DeferredGraph graph = new DeferredGraph(call);
    graph.optimize();
    graph.dumpGraph();
    throw new UnsupportedOperationException("TODO");
  }
  

  public Vector materialize(Vector root) {
    
    long start = System.nanoTime();
    
    DeferredGraph graph = new DeferredGraph(root);

    if(VectorPipeliner.DEBUG) {
      System.out.print("unopt");
      graph.dumpGraph();
    }

    graph.optimize();

    if(VectorPipeliner.DEBUG) {
      graph.dumpGraph();
    }

    // force any memoized values in the graph
    evaluate(graph);

    if(Profiler.ENABLED) {
      long time = System.nanoTime() - start;
      Profiler.materialized(time);
    }
    // return result
    return root;
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
