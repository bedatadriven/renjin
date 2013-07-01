package org.renjin.compiler.pipeline.opencl.accessor;

import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.OclKernelBody;

public class OclSquareAccessor implements OclAccessor {

  private final OclAccessor vector;

  public OclSquareAccessor(OclAccessorFactory factory, DeferredNode node) {
    this.vector = factory.get(node.getOperand(0));
  }

  public static boolean accept(DeferredNode node) {
    return node.isComputation() && node.getComputation().getComputationName().equals("sqr");
  }

  @Override
  public void init(OclKernelBody body) {
    vector.init(body);
  }

  @Override
  public String value() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String length() {
    return vector.length();
  }

  @Override
  public String valueAt(OclKernelBody body, String index) {
    String value = body.tempVar("value");
    body.printlnf("double %s = %s;", value, vector.valueAt(body, index));
    return String.format("(%s*%s)", value, value);
  }
}
