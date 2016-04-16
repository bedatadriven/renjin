package org.renjin.gcc.gimple.type;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;

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
    Preconditions.checkArgument(precision == 32 || precision == 64);
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
    if(getPrecision() == 64) {
      return 2;
    } else {
      return 1;
    }
  }

  @Override
  public Type jvmType() {
    switch (getPrecision()) {
      case 32:
        return Type.FLOAT_TYPE;
      case 64:
        return Type.DOUBLE_TYPE;
      default:
        throw new UnsupportedOperationException("Precision: " + getSize());
    }
  }

  @Override
  public int sizeOf() {
    return getSize() / 8;
  }
}
