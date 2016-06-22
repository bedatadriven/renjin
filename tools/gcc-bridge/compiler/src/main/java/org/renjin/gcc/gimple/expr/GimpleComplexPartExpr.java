package org.renjin.gcc.gimple.expr;


import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleExprVisitor;

import java.util.List;

public abstract class GimpleComplexPartExpr extends GimpleExpr {

  private GimpleExpr complexValue;

  public GimpleExpr getComplexValue() {
    return complexValue;
  }

  public void setComplexValue(GimpleExpr complexValue) {
    this.complexValue = complexValue;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findOrDescend(complexValue, predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    complexValue = replaceOrDescend(complexValue, predicate, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    visitor.visitComplexPart(this);
  }
}
