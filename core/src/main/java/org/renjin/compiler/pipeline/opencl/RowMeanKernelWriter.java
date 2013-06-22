package org.renjin.compiler.pipeline.opencl;


import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.arg.ArgumentMap;
import org.renjin.compiler.pipeline.opencl.arg.OclAccessor;
import org.renjin.compiler.pipeline.opencl.arg.OclAccessors;
import org.renjin.sexp.DoubleArrayVector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class RowMeanKernelWriter {

  private StringWriter source = new StringWriter();
  private PrintWriter w = new PrintWriter(source);

  public KernelComputation write(DeferredNode node) {

    ArgumentMap args = new ArgumentMap(node);
    args.setResultBufferType("double");

    OclAccessor matrix = OclAccessors.get(node.getOperand(0), args);
    OclAccessor rowCount = OclAccessors.get(node.getOperand(1), args);

    //  w.println("#pragma OPENCL EXTENSION cl_khr_fp64: enable");
    w.println("__kernel void rowMeans("  + args.toOpenCl() + ") {");

    // each work group handles one row mean

    // calculate the number of columns
    w.println();
    w.println("int numCols = " + matrix.length() + " / " + rowCount.value() + ";");

    // determine which row we're handling
    w.println("int row = get_global_id(0);");

    w.println("// sum over all the values in this row");
    w.println("double sum = 0;");
    w.println("int count = 0;");
    w.println("for(int i=0;i<numCols;++i) {");
    w.println("  int sourceIndex = row + (i * numRows);");
    w.println("  double value = " + matrix.valueAt("sourceIndex") + ";");
    w.println("  if(!isnan(value)) {");
    w.println("    sum = sum + value;");
    w.println("    count = count + 1;");
    w.println("  }");
    w.println("}"); // end foor loop
    w.println("result[row] = sum / (float)count;");
    w.println("}");
    w.flush();

    return new KernelComputation(args, source.toString(), "rowMeans");
  }

  @Override
  public String toString() {
    return source.toString();
  }
}
