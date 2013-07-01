package org.renjin.compiler.pipeline.opencl;

import org.jocl.*;

public class OpenClContext {
  private final cl_device_id[] devices;
  private final cl_context context;

  public OpenClContext(cl_platform_id platformId) {

    int numDevices[] = new int[1];
    CL.clGetDeviceIDs(platformId, CL.CL_DEVICE_TYPE_ALL, 0, null, numDevices);

    devices = new cl_device_id[numDevices[0]];
    CL.clGetDeviceIDs(platformId, CL.CL_DEVICE_TYPE_ALL, devices.length, devices, null);

    cl_context_properties props  = new cl_context_properties();
    props.addProperty(CL.CL_CONTEXT_PLATFORM, platformId);

    context = CL.clCreateContext(props, devices.length, devices, null, null, null);
  }

  public cl_kernel compileKernel(String source, String kernelName) {

    System.out.println(source);

    cl_program program = CL.clCreateProgramWithSource(context, 1,
        new String[]{source},
        null,
        null);

    CL.clBuildProgram(program, devices.length, devices, null, null, null);

    return CL.clCreateKernel(program, kernelName, null);
  }

  public cl_context getHandle() {
    return context;
  }

  public cl_command_queue createOrderedQueue() {
    return CL.clCreateCommandQueue(context, devices[0], 0, null);
  }
}
