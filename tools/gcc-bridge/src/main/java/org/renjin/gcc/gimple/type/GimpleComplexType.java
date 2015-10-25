package org.renjin.gcc.gimple.type;

import org.objectweb.asm.Type;

/**
 * Type representing complex numbers
 */
public class GimpleComplexType extends AbstractGimpleType {

  public GimpleComplexType() {
  }
  
  public GimpleComplexType(GimpleRealType partType) {
    setSize(partType.getSize() * 2);
  }

  @Override
  public int sizeOf() {
    return getSize() / 8;
  }

  @Override
  public String toString() {
    return "complex";
  }
  

  /**
   * 
   * @return the JVM type of this complex number's real and imaginary parts. 
   * Either {@code DOUBLE_TYPE} or {@code FLOAT_TYPE}
   */
  public Type getJvmPartType() {
    return getPartType().jvmType();
  }
  
  public Type getJvmPartArrayType() {
    return Type.getType("[" + getJvmPartType().getDescriptor());
  }
  
  public GimpleRealType getPartType() {
    return new GimpleRealType(getSize() / 2);
  }
}
