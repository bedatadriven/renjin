package org.renjin.compiler.pipeline;


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.Vector;

public class SimpleVectorPipeliner implements VectorPipeliner {
  @Override
  public Vector materialize(DeferredComputation root) {
    DeferredGraph graph = new DeferredGraph(root);

    forceMemoizedValues(graph.getRoot());

    return graph.getRoot().getVector();
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
