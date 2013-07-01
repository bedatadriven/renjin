package org.renjin.compiler.pipeline.opencl.accessor;

import org.renjin.compiler.pipeline.opencl.OclKernelBody;


public class OclBufferAccessor implements OclAccessor {
  private String kernelArgumentName;
  private String kernelLengthArgumentName;

  public OclBufferAccessor(String kernelArgumentName, String kernelLengthArgumentName) {
    this.kernelArgumentName = kernelArgumentName;
    this.kernelLengthArgumentName = kernelLengthArgumentName;
  }

  @Override
  public void init(OclKernelBody body) {

  }

  @Override
  public String value() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String length() {
    return kernelLengthArgumentName;
  }

  @Override
  public String valueAt(OclKernelBody body, String index) {
    return kernelArgumentName + "[" + index + "]";
  }
}
