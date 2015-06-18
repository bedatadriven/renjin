package org.renjin.compiler.pipeline.optimize;

import org.apache.commons.collections15.map.LRUMap;
import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.Vector;

public class AggregationRecycler implements Optimizer {

  public AggregationRecycler() {
  }

  private static LRUMap<String, DeferredNode> aggrCache = new LRUMap<String, DeferredNode>(
      1000);

  // TODO: make map size dependent on number of vector entries, not number of
  // vectors

  @Override
  public boolean optimize(DeferredGraph graph, DeferredNode node) {
    if (node.getVector() instanceof MemoizedComputation
        && !(node.getVector() instanceof CachedResultNode)) {
      String op = ((DeferredComputation) node.getVector()).getComputationName();
      // TODO: support more diverse subtrees
      if (("sum".equals(op) || "mean".equals(op) || "min".equals(op) || "max"
          .equals(op)) && node.getOperands().size() == 1) {
        String chc = node.toString();
        if (!aggrCache.containsKey(chc)) {
          aggrCache.put(chc, node);
        } else {
          // make sure this is indeed the same thing
          DeferredNode on = aggrCache.get(chc);
          if (!(on.getVector() instanceof DeferredComputation)) {
            graph.replaceNode(node,
                new CachedResultNode(node.getId(), on.getVector()));
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean isCached(String repr) {
    return aggrCache.containsKey(repr);
  }
  
  public static void clear() {
    aggrCache.clear();
  }

  public static class CachedResultNode extends DeferredNode {
    public CachedResultNode(int id, Vector vector) {
      super(id, vector);
    }

    public String getDebugLabel() {
      return super.getDebugLabel() + " (cached)";
    }

  }

}
