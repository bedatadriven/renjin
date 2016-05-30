package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.List;

public class GimpleMemRef extends GimpleLValue {

  private GimpleExpr pointer;
  private GimpleExpr offset;

  public GimpleMemRef() {
  }

  public GimpleMemRef(GimpleExpr pointer) {
    this.pointer = pointer;
    this.offset = new GimpleIntegerConstant();
    this.offset.setType(pointer.getType());
    setType(pointer.getType().getBaseType());
  }

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

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(pointer, predicate, results);
    findOrDescend(offset, predicate, results);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitMemRef(this);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    pointer = replaceOrDescend(pointer, predicate, newExpr);
    offset = replaceOrDescend(offset, predicate, newExpr);
  }
  
  public boolean isOffsetZero() {
    return offset instanceof GimpleIntegerConstant && 
        ((GimpleIntegerConstant) offset).getNumberValue().intValue() == 0;
  }
}
