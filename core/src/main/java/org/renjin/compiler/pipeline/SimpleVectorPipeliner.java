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
    } else if(vector instanceof DeferredComputation && vector instanceof DoubleVector) {
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
