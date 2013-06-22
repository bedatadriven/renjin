package org.renjin.compiler.pipeline.opencl.arg;


public abstract class OclAccessor {
  /**
   *
   * @return an OpenCL expression which evaluates to the scalar value of the node
   */
  public abstract String value();

  /**
   *
   * @return an OpenCL expression which evaluates to the length of the buffer
   */
  public abstract String length();

  /**
   *
   * @param index an OpenCL expression which evaluates to the desired index
   * @return an OpenCL expression which evaluates to the buffer's value at the
   * given index.
   */
  public abstract String valueAt(String index);
}
