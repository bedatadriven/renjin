package org.renjin.gcc.gimple.statement;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.List;
import java.util.Set;

public class GimpleConditional extends GimpleStatement {
  // gimple_cond <ne_expr, i_4, j_6, NULL, NULL>
  // goto <bb 46>;
  // else
  // goto <bb 47>;

  private GimpleOp operator;
  private List<GimpleExpr> operands;
  private int trueLabel;
  private int falseLabel;

  GimpleConditional() {
  }

  void setOperator(GimpleOp op) {
    this.operator = op;
  }

  void setOperands(List<GimpleExpr> operands) {
    this.operands = operands;
  }

  public GimpleOp getOperator() {
    return operator;
  }

  @Override
  public List<GimpleExpr> getOperands() {
    return operands;
  }

  public int getTrueLabel() {
    return trueLabel;
  }

  public void setTrueLabel(int trueLabel) {
    this.trueLabel = trueLabel;
  }

  public int getFalseLabel() {
    return falseLabel;
  }

  public void setFalseLabel(int falseLabel) {
    this.falseLabel = falseLabel;
  }

  @Override
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    findUses(operands, predicate, results);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("gimple_cond<").append(operator).append(",");

    Joiner.on(", ").appendTo(sb, operands);
    sb.append("> goto <").append("BB").append(trueLabel).append("> else goto <").append("BB").append(falseLabel)
        .append(">");
    return sb.toString();
  }

  @Override
  public Set<Integer> getJumpTargets() {
    return Sets.newHashSet(trueLabel, falseLabel);
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitConditional(this);
  }

  public GimpleExpr getOperand(int index) {
    return operands.get(index);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    replaceAll(predicate, operands, newExpr);
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    for (GimpleExpr operand : operands) {
      operand.accept(visitor);
    }
  }
}
