package org.renjin.gcc.gimple.type;


public class PointerType implements GimpleType {
  private GimpleType innerType;

  public PointerType(GimpleType innerType) {
    this.innerType = innerType;
  }

  public GimpleType getInnerType() {
    return innerType;
  }

  @Override
  public String toString() {
    return innerType.toString() + "*";
  }
}
