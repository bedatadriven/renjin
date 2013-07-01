package org.renjin.compiler.pipeline.opencl;


import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.accessor.OclAccessor;
import org.renjin.compiler.pipeline.opencl.accessor.OclAccessorFactory;
import org.renjin.compiler.pipeline.opencl.accessor.OclType;
import org.renjin.compiler.pipeline.opencl.arg.ArgumentList;
import org.renjin.sexp.Vector;

public class MeanKernel implements KernelProvider {


  private final ArgumentList argumentMap;
  private final OclAccessor vector;
  private final int vectorInputGraphIndex;

  public MeanKernel(DeferredNode rootNode) {
    argumentMap = new ArgumentList();
    argumentMap.setResultBufferType(OclType.DOUBLE);
    argumentMap.setLocalBufferType(OclType.DOUBLE);

    OclAccessorFactory accessorFactory = new OclAccessorFactory(argumentMap, rootNode);
    vector = accessorFactory.get(rootNode.getOperand(0));
    vectorInputGraphIndex = accessorFactory.getIndexOf(rootNode.getOperand(0));
  }

  @Override
  public String getKernelSource() {

    OclKernelBody body = new OclKernelBody();

    body.println("#pragma OPENCL EXTENSION cl_khr_fp64: enable");
    body.println("__kernel void mean(" + argumentMap.toOpenCl() + ") {");

    vector.init(body);

    // load shared mem
    body.println("unsigned int tid = get_local_id(0);");
    body.println("unsigned int i = get_global_id(0);");
    body.printlnf("localBuf[tid] = (i < %s) ? %s : 0;", vector.length(), vector.valueAt(body, "i"));

    // block all threads until we've copied in shared memory
    body.println("barrier(CLK_LOCAL_MEM_FENCE);");

   // do reduction in shared mem
    body.println("for(unsigned int s=1; s < get_local_size(0); s *= 2) {");
    body.println("  int index = 2 * s * tid;");
    body.println("  if (index < get_local_size(0)) {");
    body.println("   localBuf[index] += localBuf[index + s];");
    body.println("  }");
    body.println("  barrier(CLK_LOCAL_MEM_FENCE);");
    body.println("}");

    // write result for this block to global mem
    body.println("if (tid == 0) result[get_group_id(0)] = localBuf[0];");
    body.println("}");
    return body.toString();
  }

  @Override
  public String getKernelName() {
    return "mean";
  }

  public int getNumThreads(Vector[] operands) {
    int maxThreads = 16;
    int n = operands[vectorInputGraphIndex].length();
    return (n < maxThreads) ? nextPow2(n) : maxThreads;
  }

  public int getNumBlocks(Vector[] operands) {
    int threads = getNumThreads(operands);
    int n = operands[vectorInputGraphIndex].length();
    return (n + threads - 1) / threads;
  }

  @Override
  public long[] getGlobalWorkSize(Vector[] operands) {
    return new long[] { getNumBlocks(operands) * getNumThreads(operands) };
  }

  @Override
  public long[] getLocalWorkSize(Vector[] operands) {
    return new long[] { getNumThreads(operands) };
  }

  @Override
  public OclType getResultType() {
    return OclType.DOUBLE;
  }

  @Override
  public int getResultSize(Vector[] operands) {
    // each thread block / workgroup will produce a
    // partial result
    return getNumBlocks(operands);
  }

  @Override
  public double[] postProcessResult(Vector[] operands, double[] result) {
    double sum = 0;
    for(int i=0;i!=result.length;++i) {
      sum += result[i];
    }
    double length = operands[vectorInputGraphIndex].length();
    return new double[] { sum / length };
  }

  @Override
  public int getLocalSize(Vector[] operands) {
    return getNumThreads(operands);
  }

  @Override
  public ArgumentList getArgumentMap() {
    return argumentMap;
  }

  private int nextPow2(int a) {
    // http://stackoverflow.com/questions/5242533/fast-way-to-find-exponent-of-nearest-superior-power-of-2
    return a == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(a - 1);
  }
}
