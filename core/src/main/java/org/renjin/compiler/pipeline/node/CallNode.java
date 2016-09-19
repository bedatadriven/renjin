package org.renjin.compiler.pipeline.node;

import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Vector;

/**
 * A call to a compiled Fortran or C subroutine that can have multiple outputs.
 */
public class CallNode extends DeferredNode implements Runnable {

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
  public void run() {

    Vector inputs[] = new Vector[getOperands().size()];
    for (int i = 0; i < inputs.length; i++) {
      inputs[i] = getOperand(i).getVector();
    }

    call.evaluate(inputs);
  }
}
