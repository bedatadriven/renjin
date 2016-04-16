package org.renjin.gcc.gimple.type;

public class GimpleVoidType extends AbstractGimpleType {

  @Override
  public String toString() {
    return "void";
  }

  @Override
  public int sizeOf() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GimpleVoidType;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
