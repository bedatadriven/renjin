package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

public class GimpleConstantRef extends GimpleExpr {
  private GimpleConstant value;

  public GimpleConstant getValue() {
    return value;
  }

  public void setValue(GimpleConstant value) {
    this.value = value;
  }
  
  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    value = (GimpleConstant) replaceOrDescend(value, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitConstantRef(this);
  }
}
