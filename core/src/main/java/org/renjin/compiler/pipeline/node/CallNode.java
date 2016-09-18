package org.renjin.compiler.pipeline.node;

import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.repackaged.asm.Type;

/**
 * A call to a compiled Fortran or C function that can have multiple outputs.
 */
public class CallNode extends DeferredNode {

  private DeferredNativeCall call;

  public CallNode(DeferredNativeCall call) {
    super();
    this.call = call;
  }

  @Override
  public String getDebugLabel() {
    return call.getDebugName();
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.ELLIPSE;
  }

  @Override
  public Type getResultVectorType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeferredNode call() {
    throw new UnsupportedOperationException("TODO");
  }
}
