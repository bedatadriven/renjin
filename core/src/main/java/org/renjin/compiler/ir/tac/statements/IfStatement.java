/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.CmpGE;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Logical;

import java.util.Arrays;

import static org.renjin.repackaged.asm.Opcodes.*;


public class IfStatement implements Statement, BasicBlockEndingStatement {

  private Expression condition;
  private IRLabel trueTarget;
  private IRLabel falseTarget;
  private IRLabel naTarget;

  private Logical constantValue;

  public IfStatement(Expression condition, IRLabel trueTarget, IRLabel falseTarget, IRLabel naTarget) {
    this.condition = condition;
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
    this.naTarget = naTarget;
  }

  public IfStatement(Expression condition, IRLabel trueTarget, IRLabel falseTarget) {
    this.condition = condition;
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
    this.naTarget = null;
  }


  public Expression getCondition() {
    return condition;
  }

  @Override
  public Expression getRHS() {
    return condition;
  }

  public IRLabel getTrueTarget() {
    return trueTarget;
  }

  public IRLabel getFalseTarget() {
    return falseTarget;
  }

  public IfStatement setTrueTarget(IRLabel label) {
    return new IfStatement(condition, label, falseTarget, naTarget);
  }

  public IfStatement setFalseTarget(IRLabel label) {
    return new IfStatement(condition, trueTarget, label, naTarget);
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    if(naTarget == null) {
      return Arrays.asList(trueTarget, falseTarget);
    } else {
      return Arrays.asList(trueTarget, falseTarget, naTarget);
    }
  }

  @Override
  public void setRHS(Expression newRHS) {
    this.condition = newRHS;
  }

  @Override
  public String toString() {
    return "if " + condition + " => TRUE:" + trueTarget + ", FALSE:" +  falseTarget +
          ", NA:" + (naTarget == null ? "ERROR" : naTarget);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      condition = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return condition;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public Logical getConstantValue() {
    return constantValue;
  }

  public void setConstantValue(Logical constantValue) {
    this.constantValue = constantValue;
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitIf(this);
  }

  @Override
  public void emit(EmitContext emitContext, InstructionAdapter mv) {

    if(constantValue == Logical.TRUE) {
      mv.visitJumpInsn(GOTO, emitContext.getAsmLabel(trueTarget));
    }
    if(constantValue == Logical.FALSE) {
      mv.visitJumpInsn(GOTO, emitContext.getAsmLabel(falseTarget));
    }

    if(condition instanceof CmpGE) {
      CmpGE comparison = (CmpGE) condition;
      comparison.childAt(0).getCompiledExpr(emitContext).loadScalar(emitContext, mv, VectorType.INT);
      comparison.childAt(1).getCompiledExpr(emitContext).loadScalar(emitContext, mv, VectorType.INT);
      mv.visitJumpInsn(IF_ICMPLT, emitContext.getAsmLabel(falseTarget));
      mv.visitJumpInsn(GOTO, emitContext.getAsmLabel(trueTarget));

    } else if (condition instanceof LValue) {
      CompiledSexp conditionExpr = condition.getCompiledExpr(emitContext);
      conditionExpr.loadScalar(emitContext, mv, VectorType.LOGICAL);
      mv.visitJumpInsn(IFEQ, emitContext.getAsmLabel(falseTarget));
      mv.visitJumpInsn(GOTO, emitContext.getAsmLabel(trueTarget));
    }
  }

  @Override
  public boolean isPure() {
    return condition.isPure();
  }
}
