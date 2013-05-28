package org.renjin.gcc.gimple.expr;

public class GimpleMemRef extends GimpleLValue {

  private GimpleExpr pointer;

  public GimpleExpr getPointer() {
    return pointer;
  }

  public void setPointer(GimpleExpr pointer) {
    this.pointer = pointer;
  }

  public String toString() {
    return "*" + pointer;
  }

}
