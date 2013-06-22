package org.renjin.compiler.pipeline.opencl;


import org.renjin.compiler.pipeline.JittedComputation;
import org.renjin.compiler.pipeline.opencl.arg.ArgumentMap;
import org.renjin.sexp.Vector;

public class KernelComputation implements JittedComputation {


  public KernelComputation(ArgumentMap argumentMap, String kernelSource, String kernelName) {


  }

  @Override
  public double[] compute(Vector[] operands) {
    throw new UnsupportedOperationException();
  }
}
