package org.renjin.compiler.pipeline.opencl.accessor;

import org.renjin.compiler.pipeline.opencl.OclKernelBody;


public class OclScalarAccessor implements OclAccessor {
  private final String name;

  public OclScalarAccessor(String name) {
    this.name = name;
  }

  @Override
  public void init(OclKernelBody body) {
  }

  @Override
  public String value() {
    return name;
  }

  @Override
  public String length() {
    return "1";
  }

  @Override
  public String valueAt(OclKernelBody body, String index) {
    return name;
  }
}
