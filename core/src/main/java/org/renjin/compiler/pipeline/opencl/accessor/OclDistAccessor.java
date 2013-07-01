package org.renjin.compiler.pipeline.opencl.accessor;

import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.OclKernelBody;


public class OclDistAccessor implements OclAccessor {

  private OclAccessor vector;
  private String lengthVar;

  public OclDistAccessor(OclAccessorFactory oclAccessorFactory, DeferredNode node) {
    vector = oclAccessorFactory.get(node.getOperand(0));
  }
  public static boolean accept(DeferredNode node) {
    return node.isComputation() && node.getComputation().getComputationName().equals("dist");
  }

  @Override
  public void init(OclKernelBody body) {
    vector.init(body);
    lengthVar = body.tempVar("length");
    body.printlnf("int " + lengthVar + " = (%s*%s);", vector.length(), vector.length());
  }

  @Override
  public String value() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String length() {
    return lengthVar;
  }

  @Override
  public String valueAt(OclKernelBody body, String index) {

    String size = vector.length();
    String rowVar = body.tempVar("row");
    String colVar = body.tempVar("col");
    String xVal = body.tempVar("x");
    String yVal = body.tempVar("y");


    body.println("int " + rowVar + " = " + index + " % " +  size + ";");
    body.println("int " + colVar + " = " + index + " / " +  size + ";");
    body.println("double " + xVal + " = " + vector.valueAt(body, rowVar) + ";");
    body.println("double " + yVal + " = " + vector.valueAt(body, colVar) + ";");

    return "fabs(" + xVal+ " - " + yVal + ")";
  }
}
