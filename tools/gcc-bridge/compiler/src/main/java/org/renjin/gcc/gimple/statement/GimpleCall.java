package org.renjin.gcc.gimple.statement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;

import java.util.List;

public class GimpleCall extends GimpleStatement {

  private GimpleExpr function;
  private List<GimpleExpr> operands = Lists.newArrayList();
  private GimpleLValue lhs;

  public GimpleExpr getFunction() {
    return function;
  }

  @Override
  @JsonProperty("arguments")
  public List<GimpleExpr> getOperands() {
    return operands;
  }


  public GimpleExpr getOperand(int i) {
    return operands.get(i);
  }
  
  public GimpleLValue getLhs() {
    return lhs;
  }

  public void setFunction(GimpleExpr function) {
    this.function = function;
  }

  public void setLhs(GimpleLValue lhs) {
    this.lhs = lhs;
  }


  @Override
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    function.findOrDescend(predicate, results);
    for (GimpleExpr argument : operands) {
      argument.findOrDescend(predicate, results);
    }
    // if the lhs is a compound expression, such as
    //    *x  = f() or
    //    x.i = f() or
    // Re(x)  = f()
    // 
    // then we consider this a USE of x rather than a definition

    if(lhs != null && !(lhs instanceof GimpleSymbolRef)) {
      lhs.find(predicate, results);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(lhs);
    sb.append(" = ");
    sb.append("gimple_call <").append(function).append(", ");
    Joiner.on(", ").appendTo(sb, operands);
    sb.append(">");
    return sb.toString();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitCall(this);
  }

  @Override
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    if(lhs != null) {
      return predicate.apply(lhs);
    } else {
      return false;
    }
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(lhs != null) {
      if (predicate.apply(lhs)) {
        lhs = (GimpleLValue) newExpr;
      } else {
        lhs.replaceAll(predicate, newExpr);
      }
    }
    replaceAll(predicate, operands, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    if(lhs != null) {
      lhs.accept(visitor);
    }
    for (GimpleExpr operand : operands) {
      operand.accept(visitor);
    }
  }

}
