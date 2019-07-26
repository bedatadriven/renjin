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

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.pipeliner.fusion.LoopKernelCache;
import org.renjin.pipeliner.node.DeferredNode;
import org.renjin.primitives.matrix.DeferredColSums;
import org.renjin.primitives.summary.DeferredSum;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.*;

import java.util.concurrent.Executors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


public class DeferredGraphTest extends EvalTestCase {

  @Test
  public void fuseTest() {
    eval("x <- 1:1000");
    eval("y <- 1:1000 / 2");
    SEXP sum = eval("sum(x + y ^ 2)");
    
    DeferredGraph graph = new DeferredGraph((Vector)sum);
    graph.dumpGraph();
    graph.optimize(new LoopKernelCache(MoreExecutors.newDirectExecutorService()));
    graph.dumpGraph();
    
    
    VectorPipeliner pipeliner = new VectorPipeliner(Executors.newFixedThreadPool(1));
    pipeliner.evaluate(graph);
  }

  @Test
  public void fusedNodesPreserveAttributes() {

    DoubleArrayVector matrix = new DoubleArrayVector(1, 2, 3, 4);
    DeferredColSums colSums = new DeferredColSums(matrix, 2, false,
        AttributeMap.builder().setNames(new StringArrayVector("a", "b")).build());

    DeferredGraph graph = new DeferredGraph(colSums);
    graph.optimize(new LoopKernelCache(Executors.newSingleThreadExecutor()));

    DeferredGraphEval eval = new DeferredGraphEval(graph, Executors.newSingleThreadExecutor());
    eval.execute();

    Vector result = graph.getRootResult(0);


    assertThat(result.getNames(), elementsIdenticalTo(c("a", "b")));
  }

  @Test
  public void equivalentDataNodes() {


    DoubleVector a = new DoubleArrayVector(1,2);
    DoubleVector b = new DoubleArrayVector(1,2,3);

    DeferredGraph graph = new DeferredGraph();
    graph.addRoot(new DeferredSum(a, AttributeMap.EMPTY));
    graph.addRoot(new DeferredSum(a, AttributeMap.EMPTY));
    graph.addRoot(new DeferredSum(b, AttributeMap.EMPTY));

    DeferredNode sa1 = graph.getRoots().get(0);
    DeferredNode sa2 = graph.getRoots().get(1);
    DeferredNode sb = graph.getRoots().get(2);

    assertTrue(sa1 == sa2);
    assertFalse(sa1 == sb);
  }

  @Test
  public void attributesPreserved() {

    DeferredColSums colSums = new DeferredColSums(new DoubleArrayVector(1, 2, 3, 4), 2, true, AttributeMap.EMPTY);
    StringArrayVector names = new StringArrayVector("a", "b");
    SEXP namedColSums = colSums.setAttributes((AttributeMap.builder().setNames(names)));

    DeferredGraph graph = new DeferredGraph();
    graph.addRoot((Vector)namedColSums);
    graph.optimize(new LoopKernelCache(Executors.newSingleThreadExecutor()));

    DeferredGraphEval eval = new DeferredGraphEval(graph, Executors.newSingleThreadExecutor());
    eval.execute();

    Vector result = graph.getRootResult(0);
    assertThat(result.getNames(), elementsIdenticalTo(names));

  }
}