package org.renjin.gcc.gimple.expr;

public class GimpleNull extends GimpleExpr {
  public static final GimpleNull INSTANCE = new GimpleNull();

  private GimpleNull() {
  }

  @Override
  public String toString() {
    return "NULL";
  }
}
