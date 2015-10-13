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
}
