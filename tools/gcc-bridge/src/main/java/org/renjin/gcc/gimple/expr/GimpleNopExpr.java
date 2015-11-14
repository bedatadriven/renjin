package org.renjin.gcc.gimple.expr;

/**
 * No operation expression
 */
public class GimpleNopExpr extends GimpleExpr {
  private GimpleExpr value;

  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }
}
