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

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.pipeline.fusion.LoopKernelCache;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.concurrent.Executors;


public class DeferredGraphTest extends EvalTestCase {

  @Test
  public void fuseTest() {
    eval("x <- 1:1000");
    eval("y <- 1:1000 / 2");
    SEXP sum = eval("sum(x + y ^ 2)");
    
    DeferredGraph graph = new DeferredGraph((Vector)sum);
    graph.dumpGraph();
    graph.optimize(new LoopKernelCache(MoreExecutors.sameThreadExecutor()));
    graph.dumpGraph();
    
    
    VectorPipeliner pipeliner = new VectorPipeliner(Executors.newFixedThreadPool(1));
    pipeliner.evaluate(graph);
  }
  
}