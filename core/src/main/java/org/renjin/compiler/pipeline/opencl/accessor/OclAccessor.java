package org.renjin.compiler.pipeline.opencl.accessor;


import org.renjin.compiler.pipeline.opencl.OclKernelBody;

public interface OclAccessor {

  void init(OclKernelBody body);

  /**
   *
   * @return an OpenCL expression which evaluates to the scalar value of the node
   */
  String value();

  /**
   *
   * @return an OpenCL expression which evaluates to the length of the buffer
   */
  String length();

  /**
   *
   *
   * @param body
   * @param index an OpenCL expression which evaluates to the desired index
   * @return an OpenCL expression which evaluates to the buffer's value at the
   * given index.
   */
  String valueAt(OclKernelBody body, String index);
}
