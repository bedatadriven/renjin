package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.GimpleExprVisitor;

import java.util.function.Predicate;

/**
 * Don't know what this does yet...
 */
public class GimpleViewConvertExpr extends GimpleExpr {
  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    throw new UnsupportedOperationException("TODO");
  }
}
