package org.renjin.compiler.pipeline.opencl;


import com.google.common.collect.Lists;
import org.jocl.*;
import org.renjin.compiler.pipeline.JittedComputation;
import org.renjin.compiler.pipeline.opencl.accessor.OclType;
import org.renjin.compiler.pipeline.opencl.arg.ArgumentList;
import org.renjin.sexp.Vector;

import java.util.List;

public class KernelComputation implements JittedComputation {
  private final KernelProvider kernel;
  private cl_kernel kernelHandle;

  public KernelComputation(KernelProvider kernel) {
    this.kernel = kernel;
    String kernelSource = kernel.getKernelSource();
    kernelHandle = OpenCl.INSTANCE.getContext().compileKernel(kernelSource, kernel.getKernelName());

    System.out.println(kernelSource);
  }

  @Override
  public double[] compute(Vector[] operands) {
    cl_context context = OpenCl.INSTANCE.getContext().getHandle();

    cl_command_queue queue = OpenCl.INSTANCE.getContext().createOrderedQueue();

    List<cl_mem> buffers = Lists.newArrayList();
    try {
      marshallKernelArguments(context, queue, operands, buffers);

      long globalWorkSize[] = kernel.getGlobalWorkSize(operands);
      long localWorkSize[] = kernel.getLocalWorkSize(operands);

      // execute
      CL.clEnqueueNDRangeKernel(queue, kernelHandle, 1, null, globalWorkSize, localWorkSize,
          0, null, null);

      // retrieve result
      int resultSize = kernel.getResultSize(operands);
      double result[] = new double[resultSize];

      CL.clEnqueueReadBuffer(queue, buffers.get(0), CL.CL_TRUE, 0,
          kernel.getArgumentMap().getResultType().sizeOf() * resultSize,
          Pointer.to(result), 0, null, null);

      return kernel.postProcessResult(operands, result);

    } finally {
      // release buffers
      for(cl_mem buffer : buffers) {
        try {
          CL.clReleaseMemObject(buffer);
        } catch(Exception e) {
        }
      }

      try {
        CL.clReleaseCommandQueue(queue);
      } catch(Exception e) {

      }
    }
  }

  private void marshallKernelArguments(cl_context context, cl_command_queue queue, Vector[] operands,
                                               List<cl_mem> buffers) {

    // set up output buffer
    cl_mem outputBuffer = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY,
        kernel.getResultType().sizeOf() * kernel.getResultSize(operands), null, null);

    CL.clSetKernelArg(kernelHandle, 0, Sizeof.cl_mem, Pointer.to(outputBuffer));

    buffers.add(outputBuffer);

    // set up input buffers / arguments
    for(ArgumentList.InputArgument arg : kernel.getArgumentMap()) {
      Vector operand = operands[arg.getInputGraphIndex()];
      if(arg.isScalar()) {
        CL.clSetKernelArg(kernelHandle, arg.getKernelArgumentIndex(), arg.getType().sizeOf(),
            arg.getType().scalarPointerTo(operand));
      } else {
        // create a buffer to hold this argument
        int bytes = arg.getType().sizeOf() * operand.length();
        cl_mem buffer = CL.clCreateBuffer(context, CL.CL_MEM_READ_ONLY,
            bytes, null, null);

        // enqueue the buffer for transfer to GPU
        CL.clEnqueueWriteBuffer(queue, buffer, CL.CL_FALSE, 0,
            bytes,
            arg.getType().pointerTo(operand),
            0, // num events in waitlist
            null, // event wait list
            null); // event

        // link the buffer to the kernel arg
        CL.clSetKernelArg(kernelHandle, arg.getKernelArgumentIndex(), Sizeof.cl_mem, Pointer.to(buffer));

        // and pass the length of the vector as well
        CL.clSetKernelArg(kernelHandle, arg.getLengthKernelArgumentIndex(), Sizeof.cl_int,
            Pointer.to(new int[] { operand.length() }));

        buffers.add(buffer);
      }
    }
    // set the kernel arg for local memory if needed
    OclType localMemoryType = kernel.getArgumentMap().getLocalMemoryArgumentType();
    if(localMemoryType != null) {
      int localMemorySize = localMemoryType.sizeOf() * kernel.getLocalSize(operands);
      CL.clSetKernelArg(kernelHandle, kernel.getArgumentMap().getLocalMemoryArgumentIndex(),
          localMemorySize, null);
    }

  }
}
