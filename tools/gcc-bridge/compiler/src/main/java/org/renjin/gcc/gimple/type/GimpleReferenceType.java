package org.renjin.gcc.gimple.type;

public class GimpleReferenceType extends AbstractGimpleType implements GimpleIndirectType {
  private GimpleType baseType;

  public GimpleType getBaseType() {
    return baseType;
  }

  public void setBaseType(GimpleType baseType) {
    this.baseType = baseType;
  }
  
  @Override
  public boolean isPointerTo(Class<? extends GimpleType> clazz) {
    return clazz.isAssignableFrom(baseType.getClass());
  }

  @Override
  public int sizeOf() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return baseType + "&";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimpleReferenceType that = (GimpleReferenceType) o;

    return baseType.equals(that.baseType);

  }

  @Override
  public int hashCode() {
    return baseType.hashCode();
  }
}
