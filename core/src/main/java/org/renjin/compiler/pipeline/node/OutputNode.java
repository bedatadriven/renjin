package org.renjin.compiler.pipeline.node;

import org.renjin.primitives.ni.NativeOutputVector;

public class OutputNode extends DeferredNode {
  
  private NativeOutputVector vector;

  public OutputNode(int id, NativeOutputVector vector) {
    super(id);
    this.vector = vector;
  }

  @Override
  public String getDebugLabel() {
    return vector.getCall().getOutputName(vector.getOutputIndex());
  }

  @Override
  public String getShape() {
    return "box";
  }

  @Override
  public boolean equivalent(DeferredNode that) {
    if(!(that instanceof OutputNode)) {
      return false;
    }
    OutputNode thatNode = (OutputNode) that;
    return 
        this.vector.getCall() == thatNode.vector.getCall()  &&
        this.vector.getOutputIndex() == thatNode.vector.getOutputIndex();
  }
}
