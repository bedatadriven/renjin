package org.renjin.compiler.pipeline.opencl.accessor;

import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.OclKernelBody;

/**
 * Created with IntelliJ IDEA.
 * User: Alex
 * Date: 7/1/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class OclConvertingDoubleAccessor implements OclAccessor {

  private final OclAccessor vector;

  public OclConvertingDoubleAccessor(OclAccessorFactory oclAccessorFactory, DeferredNode node) {
    vector = oclAccessorFactory.get(node.getOperand(0));
  }
  public static boolean accept(DeferredNode node) {
    return node.isComputation() && node.getComputation().getComputationName().equals("as.double");
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
    return "((double)" + vector.valueAt(body, index) + ")";
  }
}
