package org.renjin.gcc.gimple.type;

import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

public class GimpleRealType extends GimplePrimitiveType {

  public GimpleRealType() {
  }

  public GimpleRealType(int precision) {
    setSize(precision);
  }

  /**
   * 
   * @return The number of bits of precision
   */
  public int getPrecision() {
    return getSize();
  }

  public void setPrecision(int precision) {
    Preconditions.checkArgument(precision > 0);
    setSize(precision);
  }

  @Override
  public String toString() {
    return "real" + getPrecision();
  }

  @Override
  public int hashCode() {
    return getSize();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GimpleRealType other = (GimpleRealType) obj;
    return getSize() == other.getSize();
  }

  @Override
  public int localVariableSlots() {
    return jvmType().getSize();
  }

  @Override
  public Type jvmType() {
    if(getPrecision() <= 32) {
      return Type.FLOAT_TYPE;
    } else {
      return Type.DOUBLE_TYPE;
    }
  }

  @Override
  public int sizeOf() {
    return getSize() / 8;
  }
}
