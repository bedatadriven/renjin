package org.renjin.gcc.gimple.expr;


import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleExprVisitor;

public class GimpleBitFieldRefExpr extends GimpleExpr {
  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitBitFieldRef(this);
  }
}
