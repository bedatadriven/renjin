package org.renjin.gcc.gimple.expr;

public class GimpleAddressOf extends GimpleExpr {

  public GimpleExpr expr;

  public GimpleAddressOf(GimpleExpr expr) {
    this.expr = expr;
  }

  public GimpleExpr getExpr() {
    return expr;
  }

  @Override
  public String toString() {
    return "&" + expr;
  }
}
