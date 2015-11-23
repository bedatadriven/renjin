package org.renjin.gcc.gimple.type;

public class GimpleArrayType extends AbstractGimpleType {
  private GimpleType componentType;
  private int lbound;
  private Integer ubound;

  public GimpleArrayType() {
  }

  public GimpleArrayType(GimplePrimitiveType componentType) {
    this.componentType = componentType;
  }
  

  public GimpleType getComponentType() {
    return componentType;
  }

  public void setComponentType(GimpleType componentType) {
    this.componentType = componentType;
  }

  public int getLbound() {
    return lbound;
  }

  public void setLbound(int lbound) {
    this.lbound = lbound;
  }

  public Integer getUbound() {
    return ubound;
  }

  public void setUbound(Integer ubound) {
    this.ubound = ubound;
  }

  @Override
  public String toString() {
    return componentType + "[]";
  }

  @Override
  public int sizeOf() {
    return componentType.sizeOf() * getElementCount();
  }

  public int getElementCount() {
    return getUbound() - getLbound() + 1;
  }
}
