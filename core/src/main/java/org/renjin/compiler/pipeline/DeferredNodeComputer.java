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
