package org.renjin.gcc.gimple.type;

import org.renjin.repackaged.asm.Type;

public class GimpleBooleanType extends GimplePrimitiveType {

  public GimpleBooleanType() {
    setSize(8);
  }

  @Override
  public String toString() {
    return "bool";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GimpleBooleanType;
  }

  @Override
  public int hashCode() {
    return 1;
  }


  @Override
  public int localVariableSlots() {
    return 1;
  }

  @Override
  public Type jvmType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public int sizeOf() {
    return 1;
  }
}
