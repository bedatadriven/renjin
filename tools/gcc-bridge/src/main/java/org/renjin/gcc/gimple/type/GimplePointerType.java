package org.renjin.gcc.gimple.type;

public class GimplePointerType extends AbstractGimpleType implements GimpleIndirectType {
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
}
