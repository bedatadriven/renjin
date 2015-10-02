package org.renjin.gcc.gimple.type;

import org.renjin.gcc.translate.type.ImType;

public class AbstractGimpleType implements GimpleType {
  private int size;
  private ImType resolvedType;

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public boolean isPointerTo(Class<? extends GimpleType> clazz) {
    return false;
  }

  @Override
  public <X extends GimpleType> X getBaseType() {
    throw new UnsupportedOperationException("this is not pointer type (" + getClass().getSimpleName() + ")");
  }

  @Override
  public ImType getResolvedType() {
    if(resolvedType == null) {
      throw new IllegalStateException("the gimple type " + this + " has not been resolved");
    }
    return resolvedType;
  }
  
  @Override
  public boolean isTypeResolved() {
    return resolvedType != null;
  }

  @Override
  public void setResolvedType(ImType resolvedType) {
    this.resolvedType = resolvedType;
  }
}
