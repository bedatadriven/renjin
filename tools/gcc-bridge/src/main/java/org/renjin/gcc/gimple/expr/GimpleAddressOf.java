package org.renjin.gcc.gimple.expr;

public class GimpleAddressOf extends GimpleExpr {

  public GimpleExpr value;

  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "&" + value;
  }
}
