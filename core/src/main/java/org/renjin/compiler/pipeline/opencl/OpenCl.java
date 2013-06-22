package org.renjin.compiler.pipeline.opencl;

import org.jocl.*;

/**
 *
 */
public class OpenCl {

  public static OpenCl INSTANCE = new OpenCl();

  /**
   * The OpenCL devices that are available
   */
  private final cl_device_id[] devices;

  /**
   * The OpenCL Context for all Renjin Sessions
   */
  private final cl_context context;

  private OpenCl() {

    CL.setExceptionsEnabled(true);

    int numPlatforms[] = new int[1];
    int status = CL.clGetPlatformIDs(0, null, numPlatforms);
    System.out.println(String.format("status = %d, numPlatforms = %d", status, numPlatforms[0]));

    cl_platform_id platformID[] = new cl_platform_id[numPlatforms[0]];
    status = CL.clGetPlatformIDs(platformID.length, platformID, null);

    int numDevices[] = new int[1];
    CL.clGetDeviceIDs(platformID[0], CL.CL_DEVICE_TYPE_ALL, 0, null, numDevices);

    devices = new cl_device_id[numDevices[0]];
    CL.clGetDeviceIDs(platformID[0], CL.CL_DEVICE_TYPE_ALL, devices.length, devices, null);

    cl_context_properties props  = new cl_context_properties();
    props.addProperty(CL.CL_CONTEXT_PLATFORM, platformID[0]);

    context = CL.clCreateContext(props, devices.length, devices, null, null, null);

  }
}
