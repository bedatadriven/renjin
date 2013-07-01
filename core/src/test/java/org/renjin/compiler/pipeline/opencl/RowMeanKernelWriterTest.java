package org.renjin.compiler.pipeline.opencl;


import com.google.common.base.Stopwatch;
import org.jocl.*;
import org.junit.Before;
import org.junit.Test;
import org.renjin.compiler.pipeline.DeferredNode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

public class RowMeanKernelWriterTest {

  private cl_kernel kernel;
  private cl_context context;
  private cl_device_id deviceId;

  @Before
  public void createKernel() {

    CL.setExceptionsEnabled(true);

    int numPlatforms[] = new int[1];
    int status = CL.clGetPlatformIDs(0, null, numPlatforms);
    System.out.println(String.format("status = %d, numPlatforms = %d", status, numPlatforms[0]));

    cl_platform_id platformID[] = new cl_platform_id[numPlatforms[0]];
    status = CL.clGetPlatformIDs(platformID.length, platformID, null);
    checkStatus(status);

    int numDevices[] = new int[1];
    CL.clGetDeviceIDs(platformID[0], CL.CL_DEVICE_TYPE_GPU, 0, null, numDevices);

    System.out.println("num devices = " + numDevices[0]);

    cl_device_id deviceIds[] = new cl_device_id[numDevices[0]];
    status = CL.clGetDeviceIDs(platformID[0], CL.CL_DEVICE_TYPE_GPU, deviceIds.length, deviceIds, null);

    deviceId = deviceIds[0];

    cl_context_properties props  = new cl_context_properties();
    props.addProperty(CL.CL_CONTEXT_PLATFORM, platformID[0]);

    context = CL.clCreateContext(props, deviceIds.length, deviceIds, null, null, null);

    String source = writeKernel();
    System.out.println(source);

    cl_program program = CL.clCreateProgramWithSource(context, 1,
        new String[]{source},
        null,
        null);

    CL.clBuildProgram(program, numDevices[0], deviceIds, null, null, null);

    kernel = CL.clCreateKernel(program, "rowMeans", null);
  }


  public String writeKernel() {

    StringWriter sw = new StringWriter();
    PrintWriter w = new PrintWriter(sw);

    //  w.println("#pragma OPENCL EXTENSION cl_khr_fp64: enable");
    w.println("__kernel void rowMeans(__global float *outputMeans, __global const float *inputMatrix, " +
        "int numRows, " +
        "int numCols) {");

    // each work group handles


    w.println("int row = get_global_id(0);");
    w.println("// sum over all the values in this row");
    w.println("float sum = 0;");
    w.println("int count = 0;");
    w.println("for(int i=0;i<numCols;++i) {");
    w.println("  int sourceIndex = row + (i * numRows);");
    w.println("  float value = inputMatrix[sourceIndex];");
//    w.println("  if(!isnan(value)) {");
    w.println("    sum = sum + value;");
    w.println("    count = count + 1;");
 //   w.println("  }");
    w.println("}"); // end foor loop
    w.println("outputMeans[row] = sum / (float)count;");
    w.println("}");
    w.flush();
    return sw.toString();
  }

  @Test
  public void test() {


    int numRows  = 64 * 100;
    int numCols = 10000;

    for(int i=0;i!=10;++i) {
      time(numRows, numCols);
    }
  }


  private void time(int numRows, int numCols) {
    float inMatrix[] = new float[numRows * numCols];
    Random random = new Random();
    for(int i=0;i!=inMatrix.length;++i) {
      inMatrix[i] = random.nextFloat();
    }

    Stopwatch stopwatch = new Stopwatch().start();

    cl_mem bufInput = CL.clCreateBuffer(context, CL.CL_MEM_READ_ONLY | CL.CL_MEM_USE_HOST_PTR,
        Sizeof.cl_float * numRows * numCols, Pointer.to(inMatrix), null);

    cl_mem bufOutput = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY,
        Sizeof.cl_float * numRows, null, null);

    cl_command_queue queue = CL.clCreateCommandQueue(context, deviceId, 0, null);

    CL.clEnqueueWriteBuffer(queue, bufInput, CL.CL_FALSE, 0,
        Sizeof.cl_float * inMatrix.length,
        Pointer.to(inMatrix),
        0, // num events in waitlist
        null, // event wait list
        null); // event

    CL.clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(bufOutput));

    CL.clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(bufInput));

    CL.clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[] { numRows }));
    CL.clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[] { numCols }));


    long globalWorkSize[] = new long[] { numRows };
    long localWorkSize[] = new long[] { 64  };

    CL.clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, localWorkSize,
        0, null, null);

    float means[] = new float[numRows];
    CL.clEnqueueReadBuffer(queue, bufOutput, CL.CL_TRUE, 0, Sizeof.cl_float * numRows,
        Pointer.to(means), 0, null, null);

    System.out.println("GPU: "+ stopwatch.elapsedMillis() + " means[1] = " + means[1]);

    CL.clReleaseMemObject(bufInput);
    CL.clReleaseMemObject(bufOutput);

    stopwatch.reset().start();

    means = computeRowMeans(numRows, inMatrix);

    System.out.println("CPU: "+ stopwatch.elapsedMillis() + " means[1] = " + means[1]);
    System.out.println(" ---- ");


  }
  private void checkStatus(int status) {
    if(status != 0) {
      throw new RuntimeException(CL.stringFor_cl_build_status(status));
    }
  }

  private float[] computeRowMeans(int numRows, float[] inMatrix) {

    float sums[] = new float[numRows];
    int counts[] = new int[numRows];
    int sourceIndex = 0;
    int rowLength = inMatrix.length / numRows;
    for(int col=0;col < rowLength; col++) {
      for(int row=0;row<numRows;row++) {
        float value = inMatrix[sourceIndex++];
        if(!Float.isNaN(value)) {
          sums[row] += value;
          counts[row] ++;
        }
      }
    }
    for(int row=0;row<numRows;row++) {
      sums[row] /= (float)counts[row];
    }
    return sums;
  }
}
