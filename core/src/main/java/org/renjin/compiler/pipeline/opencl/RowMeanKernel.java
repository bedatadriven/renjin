package org.renjin.compiler.pipeline.opencl;


import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.accessor.OclAccessor;
import org.renjin.compiler.pipeline.opencl.accessor.OclAccessorFactory;
import org.renjin.compiler.pipeline.opencl.accessor.OclType;
import org.renjin.compiler.pipeline.opencl.arg.*;
import org.renjin.sexp.Vector;

public class RowMeanKernel implements KernelProvider {

  private final ArgumentList argumentMap;
  private final OclAccessor matrix;
  private final OclAccessor rowCount;
  private final int rowCountIndex;
  private String source;

  public RowMeanKernel(DeferredNode rootNode) {
    argumentMap = new ArgumentList();
    argumentMap.setResultBufferType(OclType.DOUBLE);

    OclAccessorFactory accessorFactory = new OclAccessorFactory(argumentMap, rootNode);
    matrix = accessorFactory.get(rootNode.getOperand(0));
    rowCount = accessorFactory.get(rootNode.getOperand(1));
    rowCountIndex = accessorFactory.getIndexOf(rootNode.getOperand(1));

    composeKernelSource();
  }

  private void composeKernelSource() {

    OclKernelBody body = new OclKernelBody();

    body.println("#pragma OPENCL EXTENSION cl_khr_fp64: enable");
    body.println("__kernel void rowMeans(" + argumentMap.toOpenCl() + ") {");

    // each work item handles one row mean

    matrix.init(body);
    rowCount.init(body);

    // calculate the number of columns
    body.println();
    body.println("int numRows = " + rowCount.value() + ";");
    body.println("int numCols = " + matrix.length() + " / " + rowCount.value() + ";");

    // determine which row we're handling
    body.println("int row = get_global_id(0);");

    body.println("// sum over all the values in this row");
    body.println("double sum = 0;");
    body.println("int count = 0;");
    body.println("for(int i=0;i<numCols;++i) {");
    body.println("  int sourceIndex = row + (i * numRows);");
    body.println("  double value = " + matrix.valueAt(body, "sourceIndex") + ";");
    body.println("  if(!isnan(value)) {");
    body.println("    sum = sum + value;");
    body.println("    count = count + 1;");
    body.println("  }");
    body.println("}"); // end for loop
    body.println("result[row] = sum / (double)count;");
    body.println("}");


    source =  body.toString();
    System.out.println(source);

  }

  @Override
  public String getKernelSource() {
    return source;
  }

  @Override
  public String getKernelName() {
    return "rowMeans";
  }

  @Override
  public ArgumentList getArgumentMap() {
    return argumentMap;
  }

  @Override
  public double[] postProcessResult(Vector[] operands, double[] result) {
    return result;
  }

  @Override
  public long[] getGlobalWorkSize(Vector[] operands) {
    int numRows = getNumRows(operands);
    return new long [] { numRows };
  }

  private int getNumRows(Vector[] operands) {
    return operands[rowCountIndex].getElementAsInt(0);
  }

  @Override
  public long[] getLocalWorkSize(Vector[] operands) {
    return new long[] { 1L };
  }

  @Override
  public OclType getResultType() {
    return OclType.DOUBLE;
  }

  @Override
  public int getResultSize(Vector[] operands) {
    return getNumRows(operands);
  }

  @Override
  public int getLocalSize(Vector[] operands) {
    return 0;
  }
}
