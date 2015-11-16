package org.renjin.gcc.gimple.expr;

public class GimpleMemRef extends GimpleLValue {

  private GimpleExpr pointer;
  private GimpleExpr offset;

  public GimpleExpr getPointer() {
    return pointer;
  }

  public void setPointer(GimpleExpr pointer) {
    this.pointer = pointer;
  }

  public GimpleExpr getOffset() {
    return offset;
  }

  public void setOffset(GimpleExpr offset) {
    this.offset = offset;
  }

  public String toString() {
    if(isOffsetZero()) {
      return "*" + pointer;
    } else {
      return "*(" + pointer + "+" + offset + ")";
    }
  }

  public boolean isOffsetZero() {
    return offset instanceof GimpleIntegerConstant && 
        ((GimpleIntegerConstant) offset).getNumberValue().intValue() == 0;
  }
}
