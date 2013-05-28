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
  public String toString() {
    return baseType + "&";
  }
}
