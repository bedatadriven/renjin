package org.renjin.gcc.gimple.expr;


import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.repackaged.guava.base.Predicate;

public class GimpleBitFieldRefExpr extends GimpleExpr {
  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitBitFieldRef(this);
  }
}
