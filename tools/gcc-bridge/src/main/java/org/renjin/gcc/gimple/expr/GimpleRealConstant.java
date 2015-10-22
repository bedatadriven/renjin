package org.renjin.gcc.gimple.expr;

public class GimpleRealConstant extends GimplePrimitiveConstant {
  private double value;

  @Override
  public Double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

}
