package org.renjin.gcc.gimple.expr;

public class GimpleIndirection extends GimpleLValue {

  private final GimpleExpr pointer;

  public GimpleIndirection(GimpleExpr pointer) {
    super();
    this.pointer = pointer;
  }

  public GimpleExpr getPointer() {
    return pointer;
  }
  
  public String toString() {
    return "*" + pointer;
  }
  
}
