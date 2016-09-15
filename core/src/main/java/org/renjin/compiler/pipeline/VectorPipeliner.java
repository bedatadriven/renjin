package org.renjin.compiler.pipeline;

import org.renjin.eval.Profiler;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedDoubleVector;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Multimap;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

public class VectorPipeliner {


  public static boolean DEBUG = "true".equals(System.getProperty("renjin.vp.debug"));
  public static int MAX_DEPTH = 25;
  
  private final ExecutorService executorService;

  public VectorPipeliner(ExecutorService executorService) {
    this.executorService = executorService;
  }
  
  public static VectorPipeliner singleThreaded() {
    return new VectorPipeliner(MoreExecutors.sameThreadExecutor());
  }

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

  private void forceMemoizedValues(DeferredGraph graph) throws InterruptedException, ExecutionException {
    Multimap<DeferredNode, DeferredNode> dependencies = HashMultimap.create();
    for(DeferredNode root : graph.getRoots()) {
      findDependencies(root, root, dependencies);
    }
    
    // define set of nodes to be computed
    Set<DeferredNode> toCompute = Sets.newHashSet();
    for(DeferredNode node : graph.getNodes()) {
      if(node.isMemoized()) {
        toCompute.add(node);
      }
    }
    
    // execute in parallel
    ExecutorCompletionService<DeferredNode> service = new ExecutorCompletionService<>(executorService);

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
        //System.out.println("Completed " + computed);
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
