package org.renjin.compiler.pipeline.node;

import org.renjin.primitives.ni.DeferredNativeCall;

/**
 * A call to a compiled Fortran or C function that can have multiple outputs.
 */
public class CallNode extends DeferredNode {

  private DeferredNativeCall call;

  public CallNode(int id, DeferredNativeCall call) {
    super(id);
    this.call = call;
  }

  @Override
  public String getDebugLabel() {
    return call.getDebugName();
  }

  @Override
  public String getShape() {
    return "ellipse";
  }

  @Override
  public boolean equivalent(DeferredNode newNode) {
    throw new UnsupportedOperationException();
  }
}
