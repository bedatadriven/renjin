package org.renjin.compiler.pipeline;

import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.eval.Profiler;
import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedDoubleVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class VectorPipeliner {


  public static boolean DEBUG = "true".equals(System.getProperty("renjin.vp.debug"));
  public static int MAX_DEPTH = 25;
  
  private final ExecutorService executorService;

  public VectorPipeliner(ExecutorService executorService) {
    this.executorService = executorService;
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
    try {
      forceMemoizedValues(graph);
    } catch (InterruptedException | ExecutionException e) {
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
  
  public void evaluate(DeferredGraph graph)  {
    for (DeferredNode deferredNode : graph.getRoots()) {
      deferredNode.call();
    }
  }
  
  private void forceMemoizedValues(DeferredGraph graph) throws InterruptedException, ExecutionException {

    
    throw new UnsupportedOperationException();
//    Multimap<DeferredNode, DeferredNode> dependencies = HashMultimap.create();
//    for(DeferredNode root : graph.getRoots()) {
//      findDependencies(root, root, dependencies);
//    }
//    
//    // define set of nodes to be computed
//    Set<DeferredNode> toCompute = Sets.newHashSet();
//    for(DeferredNode node : graph.getNodes()) {
//      if(node.isCall() || node.isMemoized()) {
//        toCompute.add(node);
//      }
//    }
//    
//    // execute in parallel
//    ExecutorCompletionService<DeferredNode> service = new ExecutorCompletionService<>(executorService);
//
//    int running = 0;
//    while(!toCompute.isEmpty() || running > 0) {
//
//      // queue all memoized values with no remaining dependencies
//      Iterator<DeferredNode> it = toCompute.iterator();
//      while(it.hasNext()) {
//        DeferredNode node = it.next();
//        if(allComputed(dependencies.get(node))) {
//          if(VectorPipeliner.DEBUG) {
//            System.out.println("Starting " + node);
//          }
//          service.submit(new DeferredNodeComputer(node), node);
//          running ++;
//          it.remove();
//        }
//      }
//      DeferredNode computed = service.take().get();
//      running --;
//      if(VectorPipeliner.DEBUG) {
//        //System.out.println("Completed " + computed);
//      }
//    }
  }

//  private boolean allComputed(Collection<DeferredNode> deferredNodes) {
//    for(DeferredNode node : deferredNodes) {
//      if(!node.isComputed()) {
//        return false;
//      }
//    }
//    return true;
//  }
  
}
