package org.renjin.compiler.pipeline.opencl.accessor;

import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.OclKernelBody;


public class OclTransposeAccessor implements OclAccessor {
  private final OclAccessor source;
  private final OclAccessor sourceRows;
  private String sourceColCount;

  public OclTransposeAccessor(OclAccessorFactory oclAccessorFactory, DeferredNode node) {
    this.source = oclAccessorFactory.get(node.getOperand(0));
    this.sourceRows = oclAccessorFactory.get(node.getOperand(1));
  }

  public static boolean accept(DeferredNode node) {
    return node.isComputation() && node.getComputation().getComputationName().equals("t");
  }

  @Override
  public void init(OclKernelBody body) {
    source.init(body);
    sourceRows.init(body);
    sourceColCount = body.tempVar("sourceColCount");
    body.println("int " + sourceColCount + " = "  + source.length() + " / " + sourceRows.value() + ";");
  }

  @Override
  public String value() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String length() {
    return source.length();
  }

  @Override
  public String valueAt(OclKernelBody body, String index) {
    // first we have to translate this index to the row/col of the underlying matrix
    String row = body.tempVar("row");
    String col = body.tempVar("col");
    String vectorIndex = body.tempVar("vectorIndex");

    body.println("int " + row + " = " + index + " % " + sourceColCount + ";");
    body.println("int " + vectorIndex + " = (" + index + " - " + row + ") / " + sourceColCount + ";");
    body.println("int " + col + " = " + vectorIndex + " % " + sourceRows.value() + ";");

    // now compute the source index, flipping row / col
    body.printlnf("%s = (%s + (%s * %s));", vectorIndex, col, row, sourceRows.value());

    return source.valueAt(body, vectorIndex);
  }
}
