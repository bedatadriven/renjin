package org.renjin.gcc.gimple.expr;

public class GimpleConstantRef extends GimpleExpr {
  private GimpleConstant value;

  public GimpleConstant getValue() {
    return value;
  }

  public void setValue(GimpleConstant value) {
    this.value = value;
  }
  
  @Override
  public String toString() {
    return value.toString();
  }
}
