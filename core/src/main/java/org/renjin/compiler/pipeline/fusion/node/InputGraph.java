package org.renjin.compiler.pipeline.fusion.node;

import org.renjin.compiler.pipeline.node.DeferredNode;

import java.util.List;

public class InputGraph {
  private final DeferredNode root;
  private final List<DeferredNode> flattened;

  public InputGraph(DeferredNode root) {
    this.root = root;
    this.flattened = root.flatten(null);
  }

  public int getOperandIndex(DeferredNode node) {
    return flattened.indexOf(node);
  }
}
