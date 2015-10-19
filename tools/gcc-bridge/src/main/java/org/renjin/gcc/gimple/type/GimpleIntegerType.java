package org.renjin.gcc.gimple.type;

import org.objectweb.asm.Type;

public class GimpleIntegerType extends GimplePrimitiveType {
  private int precision;
  private boolean unsigned;
  
  public GimpleIntegerType() {
    
  }
  
  public GimpleIntegerType(int precision) {
    this.precision = precision;
    setSize(precision);
  }

  /**
   * 
   * @return The number of bits of precision
   */
  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public boolean isUnsigned() {
    return unsigned;
  }

  public void setUnsigned(boolean unsigned) {
    this.unsigned = unsigned;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    if (unsigned) {
      s.append("unsigned ");
    }
    s.append("int" + precision);
    return s.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + precision;
    result = prime * result + (unsigned ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GimpleIntegerType other = (GimpleIntegerType) obj;
    if (precision != other.precision)
      return false;
    if (unsigned != other.unsigned)
      return false;
    return true;
  }

  @Override
  public int localVariableSlots() {
    if(precision > 32) {
      return 2;
    } else {
      return 1;
    }
  }

  @Override
  public Type jvmType() {
    if(precision == 64) {
      return Type.LONG_TYPE;
      
    } else if(precision == 8) {
      return Type.BYTE_TYPE;
      
    } else {
      return Type.INT_TYPE;
    }
  }

  @Override
  public int sizeOf() {
    return Math.max(1, precision / 8);
  }
}
