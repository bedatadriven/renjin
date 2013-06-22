package org.renjin.compiler.pipeline.opencl.arg;

public enum OclType {
  DOUBLE;

  String getTypeName() {
    return name().toLowerCase();
  }
}
