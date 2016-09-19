package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.compiler.pipeline.node.FunctionNode;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

public class Optimizers {
  List<Optimizer> optimizers = Lists.newArrayList();

  public Optimizers() {
    if (System.getProperty("renjin.vp.disableopt") == null) {
      optimizers.add(new SquareOptimizer());
      optimizers.add(new IdentityRemover());
      optimizers.add(new AttributeRemover());
      optimizers.add(new AggregationRecycler());
    } else {
      System.err.println("Optimizers are disabled");
    }
  }

  public void optimize(DeferredGraph graph) {
    boolean changed;
    do {
      changed = false;
      List<DeferredNode> nodes = Lists.newArrayList(graph.getNodes());
      for(DeferredNode node : nodes) {
        if(node instanceof FunctionNode) {
          FunctionNode computationNode = (FunctionNode) node;
          for (Optimizer optimizer : optimizers) {
            changed |= optimizer.optimize(graph, computationNode);
          }
        }
      }
    } while(changed);
  }
}
