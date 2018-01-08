/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.pipeliner.optimize;

import org.renjin.pipeliner.DeferredGraph;
import org.renjin.pipeliner.node.DeferredNode;
import org.renjin.pipeliner.node.FunctionNode;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

public class Optimizers {
  List<Optimizer> optimizers = Lists.newArrayList();

  public Optimizers() {
    if (System.getProperty("renjin.vp.disableopt") == null) {
      optimizers.add(new SquareOptimizer());
      optimizers.add(new IdentityRemover());
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
