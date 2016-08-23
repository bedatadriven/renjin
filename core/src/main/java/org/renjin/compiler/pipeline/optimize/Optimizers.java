package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

public class Optimizers {
  List<Optimizer> optimizers = Lists.newArrayList();

  public Optimizers() {
    optimizers.add(new SquareOptimizer());
    optimizers.add(new IdentityRemover());
    optimizers.add(new AttributeRemover());
  }

  public void optimize(DeferredGraph graph) {
    boolean changed;
    do {
      changed = false;
      List<DeferredNode> nodes = Lists.newArrayList(graph.getNodes());
      for(DeferredNode node : nodes) {
        for(Optimizer optimizer : optimizers) {
          changed |= optimizer.optimize(graph, node);
        }
      }
    } while(changed);
  }
}
