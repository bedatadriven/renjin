package org.renjin.gcc.gimple.expr;

import com.google.common.base.Predicate;

import java.util.List;

public class GimpleAddressOf extends GimpleExpr {

  public GimpleExpr value;

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
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(predicate.apply(value)) {
      value = replacement;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "&" + value;
  }
}
