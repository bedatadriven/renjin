package org.renjin.gcc.gimple.expr;


import com.google.common.base.Predicate;

import java.util.Set;

public abstract class GimpleComplexPartExpr extends GimpleExpr {

  private GimpleExpr complexValue;

  public GimpleExpr getComplexValue() {
    return complexValue;
  }

  public void setComplexValue(GimpleExpr complexValue) {
    this.complexValue = complexValue;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, Set<GimpleExpr> results) {
    findOrDescend(complexValue, predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(predicate.apply(complexValue)) {
      complexValue = replacement;
      return true;
    } else {
      return false;
    }
  }
}
