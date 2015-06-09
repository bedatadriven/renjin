package org.renjin.compiler.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Vector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;


public class MultiThreadedVectorPipeliner implements VectorPipeliner {

  private final ExecutorService executorService;

  public MultiThreadedVectorPipeliner(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public Vector materialize(DeferredComputation root) {
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
