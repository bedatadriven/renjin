package org.renjin.gcc.gimple.type;

import org.objectweb.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

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

  @Override
  public void setSize(int size) {
    Preconditions.checkArgument(size == 64 || size == 128, "Invalid size: " + size);
    super.setSize(size);
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

  public boolean hasValidSize() {
    return getSize() == 64 || getSize() == 128;
  }

  @Override
  public boolean equals(Object other) {
    if(!(other instanceof GimpleComplexType)) {
      return false;
    }
    GimpleComplexType otherType = (GimpleComplexType) other;
    
    return getSize() == otherType.getSize();
  }

  @Override
  public int hashCode() {
    return getSize();
  }
}
