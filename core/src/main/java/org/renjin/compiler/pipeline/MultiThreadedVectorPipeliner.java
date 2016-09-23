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
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Multimap;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.Vector;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;


public class MultiThreadedVectorPipeliner implements VectorPipeliner {

  private final ExecutorService executorService;

  public MultiThreadedVectorPipeliner(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public Vector materialize(DeferredComputation root) {
    
    long start = System.nanoTime();
    
    DeferredGraph graph = new DeferredGraph(root);

    if(VectorPipeliner.DEBUG) {
      graph.dumpGraph();
    }

    // force any memoized values in the graph
    try {
      forceMemoizedValues(graph);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }

    if(Profiler.ENABLED) {
      long time = System.nanoTime() - start;
      Profiler.materialized(time);
    }
    // return result
    return root;
  }

  @Override
  public Vector simplify(DeferredComputation root) {
    DeferredGraph graph = new DeferredGraph(root);

    if(VectorPipeliner.DEBUG) {
      System.err.println("simplify");
      graph.dumpGraph();
    }

    return materialize(root);
  }

  private void forceMemoizedValues(DeferredGraph graph) throws InterruptedException, ExecutionException {
    Multimap<DeferredNode, DeferredNode> dependencies = HashMultimap.create();
    findDependencies(graph.getRoot(), graph.getRoot(), dependencies);

    // define set of nodes to be computed
    Set<DeferredNode> toCompute = Sets.newHashSet();
    for(DeferredNode node : graph.getNodes()) {
      if(node.isMemoized()) {
        toCompute.add(node);
      }
    }

    // execute in parallel
    ExecutorCompletionService<DeferredNode> service = new ExecutorCompletionService<DeferredNode>(executorService);

    int running = 0;
    while(!toCompute.isEmpty() || running > 0) {

      // queue all memoized values with no remaining dependencies
      Iterator<DeferredNode> it = toCompute.iterator();
      while(it.hasNext()) {
        DeferredNode node = it.next();
        if(allComputed(dependencies.get(node))) {
          if(VectorPipeliner.DEBUG) {
            System.out.println("Starting " + node);
          }
          service.submit(new DeferredNodeComputer(node), node);
          running ++;
          it.remove();
        }
      }
      DeferredNode computed = service.take().get();
      running --;
      if(VectorPipeliner.DEBUG) {
        System.out.println("Completed " + computed);
      }
    }
  }

  private boolean allComputed(Collection<DeferredNode> deferredNodes) {
    for(DeferredNode node : deferredNodes) {
      if(!node.isComputed()) {
        return false;
      }
    }
    return true;
  }

  private void findDependencies(DeferredNode parentMemo, DeferredNode node, Multimap<DeferredNode, DeferredNode> dependencies) {
    for(DeferredNode child : node.getOperands()) {
      if(child.isMemoized()) {
        dependencies.put(parentMemo, child);
        findDependencies(child, child, dependencies);
      } else if(child.isComputation()) {
        findDependencies(parentMemo, child, dependencies);
      }
    }
  }
}
