package org.renjin.gcc.gimple.expr;


import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

public class GimpleBitFieldRefExpr extends GimpleExpr {

  private GimpleExpr value;
  private int size;
  private int offset;


  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitBitFieldRef(this);
  }

  @Override
  public String toString() {
    return value + "[" + offset + ":" + size + "]";
  }
}
