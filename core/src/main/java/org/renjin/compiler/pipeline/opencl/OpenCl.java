package org.renjin.compiler.pipeline.opencl;

import org.jocl.*;

/**
 *
 */
public class OpenCl {

  public static OpenCl INSTANCE = new OpenCl();

  /**
   * The OpenCL Context for all Renjin Sessions
   */
  private final OpenClContext context;

  private OpenCl() {

    CL.setExceptionsEnabled(true);

    int numPlatforms[] = new int[1];
    CL.clGetPlatformIDs(0, null, numPlatforms);

    cl_platform_id platformID[] = new cl_platform_id[numPlatforms[0]];
    CL.clGetPlatformIDs(platformID.length, platformID, null);

    context = new OpenClContext(platformID[0]);
  }

  public OpenClContext getContext() {
    return context;
  }
}
