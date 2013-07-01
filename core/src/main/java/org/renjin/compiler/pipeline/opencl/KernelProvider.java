package org.renjin.compiler.pipeline.opencl;

import org.renjin.compiler.pipeline.opencl.arg.ArgumentList;
import org.renjin.compiler.pipeline.opencl.accessor.OclType;
import org.renjin.sexp.Vector;


public interface KernelProvider {
  String getKernelSource();

  String getKernelName();

  long[] getGlobalWorkSize(Vector[] operands);

  long[] getLocalWorkSize(Vector[] operands);

  OclType getResultType();

  int getResultSize(Vector[] operands);

  int getLocalSize(Vector[] operands);

  public ArgumentList getArgumentMap();

  double[] postProcessResult(Vector[] operands, double[] result);
}
