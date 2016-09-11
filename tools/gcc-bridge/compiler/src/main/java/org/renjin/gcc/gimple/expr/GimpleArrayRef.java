package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.List;

public class GimpleArrayRef extends GimpleLValue {
  private GimpleExpr array;
  private GimpleExpr index;

  public GimpleArrayRef() {
  }
  
  public GimpleArrayRef(GimpleExpr array, int index) {
    this.array = array;
    this.index = new GimpleIntegerConstant(GimpleIntegerType.unsigned(32), index);
    this.setType(((GimpleArrayType) array.getType()).getComponentType());
  }

  public GimpleArrayRef(GimpleExpr array, GimpleExpr index) {
    this.array = array;
    this.index = index;
  }

  public GimpleExpr getArray() {
    return array;
  }

  public void setValue(GimpleExpr value) {
    this.array = value;
  }

  public void setIndex(GimpleExpr index) {
    this.index = index;
  }

  public GimpleExpr getIndex() {
    return index;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(array, predicate, results);
    findOrDescend(index, predicate, results);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitArrayRef(this);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    array = replaceOrDescend(array, predicate, newExpr);
    index = replaceOrDescend(index, predicate, newExpr);
  }

  @Override
  public String toString() {
    return array + "[" + index + "]";
  }
}
