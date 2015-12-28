package org.renjin.gcc.gimple.statement;

import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.util.Collections;
import java.util.List;

public class GimpleReturn extends GimpleStatement {
  private GimpleExpr value;

  public GimpleReturn() {
  }

  public GimpleReturn(GimpleExpr value) {
    this.value = value;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  public GimpleExpr getValue() {
    return value;
  }

  @Override
  public List<GimpleExpr> getOperands() {
    if(value == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(value);
    }
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
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    if(value != null) {
      value.findOrDescend(predicate, results);
    }
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(predicate.apply(value)) {
      value = replacement;
      return true;
    } else if(value.replace(predicate, replacement)) {
      return true;
    }
    return false;
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.apply(value)) {
      value = newExpr;
    }
  }
}
