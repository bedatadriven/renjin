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
import org.renjin.repackaged.guava.cache.Cache;
import org.renjin.repackaged.guava.cache.CacheBuilder;

public class AggregationRecycler implements Optimizer {

  public AggregationRecycler() {
  }

  private static Cache<String, DeferredNode> aggrCache =
      CacheBuilder.newBuilder()
      .maximumSize(1000)
      .build();
  
  // TODO: make map size dependent on number of vector entries, not number of
  // vectors

  @Override
  public boolean optimize(DeferredGraph graph, FunctionNode node) {
//    if (node.getVector() instanceof MemoizedComputation
//            && !(node.getVector() instanceof CachedResultNode)) {
//      String op = ((DeferredComputation) node.getVector()).getComputationName();
//      // TODO: support more diverse subtrees
//      if (("sum".equals(op) || "mean".equals(op) || "min".equals(op) || "max"
//              .equals(op)) && node.getOperands().size() == 1) {
//        
//        String chc = node.toString();
//        DeferredNode on = aggrCache.getIfPresent(chc);
//        
//        if (on == null) {
//          aggrCache.put(chc, node);
//        } else {
////          // make sure this is indeed the same thing
////          if (!(on.getVector() instanceof DeferredComputation)) {
////            node.setre(on.getVector());
////            ((MemoizedComputation) node.getVector()).setResult(on.getVector());
////            graph.replaceNode(node,
////                    new CachedResultNode(node.getId(), on.getVector()));
////            return true;
////          }
//        }
//      }
//    }
    return false;
  }

  public static boolean isCached(String repr) {
    return aggrCache.getIfPresent(repr) != null;
  }

  public static void clear() {
    aggrCache.invalidateAll();
  }

//  public static class CachedResultNode extends DeferredNode {
//    public CachedResultNode(int id, Vector vector) {
//      super(id, vector);
//    }
//
//    public String getDebugLabel() {
//      return super.getDebugLabel() + " (cached)";
//    }
//
//    @Override
//    public String getShape() {
//      return false;
//    }
//
//    @Override
//    public boolean equivalent(DeferredNode newNode) {
//      throw new UnsupportedOperationException();
//    }
//
//  }

}
