package org.renjin.gcc.gimple.type;

import java.util.Objects;

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
  

  public int getElementCount() {
    return getSize() / componentType.getSize();
  }
  
  public boolean isStatic() {
    return true;
  }


  @Override
  public int sizeOf() {
    return getSize() / 8;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimpleArrayType other = (GimpleArrayType) o;

    if (lbound != other.lbound) {
      return false;
    }
    if (!Objects.equals(componentType, other.componentType)) {
      return false;
    }
    if (!Objects.equals(ubound, other.ubound)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = componentType.hashCode();
    result = 31 * result + lbound;
    result = 31 * result + (ubound != null ? ubound.hashCode() : 0);
    return result;
  }
}
