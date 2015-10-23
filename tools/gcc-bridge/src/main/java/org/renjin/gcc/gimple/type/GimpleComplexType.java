package org.renjin.gcc.gimple.type;

import org.objectweb.asm.Type;

/**
 * Type representing complex numbers
 */
public class GimpleComplexType extends AbstractGimpleType {
  
  @Override
  public int sizeOf() {
    return getSize();
  }

  @Override
  public String toString() {
    return "complex";
  }
  
  public int getPrecision() {
    return sizeOf() / 2;
  }

  /**
   * 
   * @return the JVM type of this complex number's real and imaginary parts. 
   * Either {@code DOUBLE_TYPE} or {@code FLOAT_TYPE}
   */
  public Type getJvmPartType() {
    if(getPrecision() == 64) {
      return Type.DOUBLE_TYPE;
    } else {
      return Type.FLOAT_TYPE;
    }
  }
  
  public Type getJvmPartArrayType() {
    return Type.getType("[" + getJvmPartType().getDescriptor());
  }
  
  public GimpleType getPartType() {
    return new GimpleRealType(getPrecision());
  }
}
