package org.renjin.compiler.pipeline.node;

import org.renjin.primitives.ni.NativeOutputVector;

public class OutputNode extends DeferredNode {
  
  private NativeOutputVector vector;

  public OutputNode(NativeOutputVector vector) {
    super();
    this.vector = vector;
  }

  @Override
  public String getDebugLabel() {
    return vector.getCall().getOutputName(vector.getOutputIndex());
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.BOX;
  }

  @Override
  public DeferredNode call() {
    throw new UnsupportedOperationException("TODO");
  }
}
