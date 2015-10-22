package org.renjin.gcc.gimple.ins;

import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;

public class GimpleReturn extends GimpleIns {
  private GimpleExpr value;

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  public GimpleExpr getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "gimple_return <" + value + ">";
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitReturn(this);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.apply(value)) {
      value = newExpr;
    }
  }
}
