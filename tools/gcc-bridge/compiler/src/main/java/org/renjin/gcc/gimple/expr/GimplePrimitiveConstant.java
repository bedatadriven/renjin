package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

/**
 * Superclass of GimpleConstants storing a primitive value
 */
public abstract class GimplePrimitiveConstant extends GimpleConstant {

  public abstract Number getValue();

  public Number getNumberValue() {
    return getValue();
  }

  @Override
  public String toString() {
    return getValue().toString();
  }


  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitPrimitiveConstant(this);
  }


  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    // NOOP: leaf node
  }
}
