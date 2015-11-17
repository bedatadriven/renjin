package org.renjin.gcc.gimple.expr;

import com.google.common.base.Predicate;

import java.util.Set;

/**
 * No operation expression
 */
public class GimpleNopExpr extends GimpleExpr {
  private GimpleExpr value;

  public GimpleExpr getValue() {
    return value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, Set<GimpleExpr> results) {
    findOrDescend(value, predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(predicate.apply(value)) {
      value = replacement;
      return true;
    } else {
      return false;
    }
  }
}
