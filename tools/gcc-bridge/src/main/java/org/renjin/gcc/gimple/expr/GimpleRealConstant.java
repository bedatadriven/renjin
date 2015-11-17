package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.type.GimpleRealType;

public class GimpleRealConstant extends GimplePrimitiveConstant {
  private double value;

  public GimpleRealConstant() {
  }

  public GimpleRealConstant(GimpleRealType type, double value) {
    setType(type);
    this.value = value;
  }

  @Override
  public Double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

}
