package org.renjin.gcc.gimple.ins;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;

import java.util.List;

public class GimpleAssign extends GimpleIns {
  private GimpleOp operator;
  private GimpleLValue lhs;
  private List<GimpleExpr> operands = Lists.newArrayList();

  public GimpleAssign() {

  }

  GimpleAssign(GimpleOp op, GimpleLValue lhs, List<GimpleExpr> arguments) {
    this.operator = op;
    this.lhs = lhs;
    this.operands = arguments;
  }

  public GimpleOp getOperator() {
    return operator;
  }

  public void setOperator(GimpleOp op) {
    this.operator = op;
  }

  public GimpleLValue getLHS() {
    return lhs;
  }

  public List<GimpleExpr> getOperands() {
    return operands;
  }

  public void setLhs(GimpleLValue lhs) {
    this.lhs = lhs;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("gimple_assign<").append(operator).append(", ").append(lhs).append(", ");
    Joiner.on(", ").appendTo(sb, operands);
    sb.append(">");
    return sb.toString();
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitAssignment(this);
  }

  @Override
  public boolean lhsMatches(Predicate<? super GimpleLValue> predicate) {
    return predicate.apply(lhs);
  }


  @Override
  public Integer getLineNumber() {
    return lhs.getLine();
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.apply(lhs)) {
      lhs = (GimpleLValue) newExpr;
    }
    replaceAll(predicate, operands, newExpr);
  }
}
