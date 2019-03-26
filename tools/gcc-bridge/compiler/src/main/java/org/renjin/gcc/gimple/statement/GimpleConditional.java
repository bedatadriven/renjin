/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.gimple.statement;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import java.util.function.Predicate;
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
    sb.append("if(").append(operator.format(operands)).append(") then ");
    sb.append(trueLabel);
    sb.append("else ").append(falseLabel);
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
