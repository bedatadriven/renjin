package org.renjin.gcc.gimple.expr;


public class GimpleArrayRef extends GimpleLValue {
  private GimpleVar var;
  private GimpleExpr index;

  public GimpleArrayRef(GimpleVar var, GimpleExpr index) {
    this.var = var;
    this.index = index;
  }

  @Override
  public String toString() {
    return var + "[" + index + "]";
  }
}
