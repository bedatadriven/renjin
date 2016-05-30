package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.repackaged.guava.base.Predicate;

import java.util.List;

public class GimpleAddressOf extends GimpleExpr {

  public GimpleExpr value;

  public GimpleAddressOf() {
  }
  
  public GimpleAddressOf(GimpleExpr value) {
    this.value = value;
    setType(new GimplePointerType(value.getType()));
  }

  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(value, predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    value = replaceOrDescend(value, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitAddressOf(this);
  }

  @Override
  public String toString() {
    return "&" + value;
  }
}
