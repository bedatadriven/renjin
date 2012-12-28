package org.renjin.gcc.gimple.expr;


public class GimpleArrayRef extends GimpleLValue {
  private GimpleExpr var;
  private GimpleExpr index;

  public GimpleArrayRef(GimpleExpr var, GimpleExpr index) {
    this.var = var;
    this.index = index;
  }

  public GimpleExpr getVar() {
    return var;
  }

  public GimpleExpr getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return var + "[" + index + "]";
  }
}
