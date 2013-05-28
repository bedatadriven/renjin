package org.renjin.gcc.gimple.expr;

public class GimpleRealConstant extends GimpleConstant {
  private double value;

  @Override
  public Double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

}
