package org.renjin.gcc.gimple.expr;

import com.google.common.base.Predicate;

import java.util.Set;

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

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, Set<GimpleExpr> results) {
    findOrDescend(pointer, predicate, results);
    findOrDescend(offset, predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(predicate.apply(pointer)) {
      pointer = replacement;
      return true;
    } else if(predicate.apply(offset)) {
      offset = replacement;
      return true;
    } else {
      return false;
    }
  }

  public boolean isOffsetZero() {
    return offset instanceof GimpleIntegerConstant && 
        ((GimpleIntegerConstant) offset).getNumberValue().intValue() == 0;
  }
}
