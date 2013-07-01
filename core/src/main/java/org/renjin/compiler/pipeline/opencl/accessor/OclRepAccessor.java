package org.renjin.compiler.pipeline.opencl.accessor;


import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.accessor.Accessors;
import org.renjin.compiler.pipeline.opencl.OclKernelBody;

public class OclRepAccessor implements OclAccessor {

  private final OclAccessor source;
  private final OclAccessor times;
  private final OclAccessor each;

  public OclRepAccessor(OclAccessorFactory oclAccessorFactory, DeferredNode node) {
    this.source = oclAccessorFactory.get(node.getOperand(0));
    this.times = oclAccessorFactory.get(node.getOperand(1));
    this.each = oclAccessorFactory.get(node.getOperand(2));
  }

  public static boolean accept(DeferredNode node) {
    return node.isComputation() && node.getComputation().getComputationName().equals("rep");
  }

  @Override
  public void init(OclKernelBody body) {
    source.init(body);
    times.init(body);
    each.init(body);
  }

  @Override
  public String value() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String length() {
    return String.format("(%s*%s*%s)", source.length(), times.value(), each.value());
  }

  @Override
  public String valueAt(OclKernelBody body, String index) {
    String sourceIndex = body.tempVar("sourceIndex");
    body.printlnf("int %s = %s %% %s;", sourceIndex, index, source.length());
    return sourceIndex;
  }
}
