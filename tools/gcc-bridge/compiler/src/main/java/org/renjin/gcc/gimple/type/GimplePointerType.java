package org.renjin.gcc.gimple.type;

public class GimplePointerType extends AbstractGimpleType implements GimpleIndirectType {
  
  public static final int SIZE_OF = 4;
  
  private GimpleType baseType;

  public GimplePointerType() {
    
  }
  
  public GimplePointerType(GimpleType baseType) {
    this.baseType = baseType;
  }

  @Override
  public <X extends GimpleType> X getBaseType() {
    return (X) baseType;
  }

  @Override
  public int sizeOf() {
    // We require the generated gimple to be compiled for 32-bit platforms so we get 32 bit pointers.
    return 4;
  }

  public void setBaseType(GimpleType baseType) {
    this.baseType = baseType;
  }

  @Override
  public String toString() {
    return baseType.toString() + "*";
  }

  @Override
  public boolean isPointerTo(Class<? extends GimpleType> clazz) {
    return clazz.isAssignableFrom(baseType.getClass());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GimplePointerType that = (GimplePointerType) o;

    return baseType.equals(that.baseType);

  }

  @Override
  public int hashCode() {
    return baseType.hashCode();
  }
}
