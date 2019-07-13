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
package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Logical;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.renjin.repackaged.asm.Opcodes.GOTO;


public class IfStatement extends Statement implements BasicBlockEndingStatement {

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
    List<IRLabel> targets = new ArrayList<>(3);
    targets.add(trueTarget);
    if(falseTarget != null) {
      targets.add(falseTarget);
    }
    if(naTarget != null) {
      targets.add(naTarget);
    }
    return targets;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("if ").append(condition).append(" goto ").append(trueTarget);
    s.append(" else ").append(falseTarget);

    if(naTarget != null) {
      s.append(" else na ").append(naTarget);
    }
    return s.toString();
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

  public GotoStatement isConstant() {
    if(constantValue == Logical.TRUE) {
      return new GotoStatement(trueTarget);
    }
    if(constantValue == Logical.FALSE) {
      return new GotoStatement(falseTarget);
    }
    if(condition.getValueBounds().isConstant()) {
      if(condition.getValueBounds().isConstantFlagEqualTo(true)) {
        return new GotoStatement(trueTarget);
      } else {
        return new GotoStatement(falseTarget);
      }
    }
    return null;
  }

  @Override
  public void emit(EmitContext emitContext, InstructionAdapter mv) {

    if(constantValue == Logical.TRUE) {
      mv.visitJumpInsn(GOTO, emitContext.getBytecodeLabel(trueTarget));
      return;
    }
    if(constantValue == Logical.FALSE && falseTarget != null) {
      mv.visitJumpInsn(GOTO, emitContext.getBytecodeLabel(falseTarget));
      return;
    }

    if(condition.getValueBounds().isConstant()) {
      if(condition.getValueBounds().isConstantFlagEqualTo(true)) {
        mv.goTo(emitContext.getBytecodeLabel(trueTarget));
      } else if (falseTarget != null){
        mv.goTo(emitContext.getBytecodeLabel(falseTarget));
      }
    }

    CompiledSexp conditionExpr = condition.getCompiledExpr(emitContext);
    conditionExpr.jumpIf(emitContext, mv,
        emitContext.getBytecodeLabel(trueTarget),
        Optional.ofNullable(naTarget).map(emitContext::getBytecodeLabel));


    mv.goTo(emitContext.getBytecodeLabel(falseTarget));
  }

  @Override
  public boolean isPure() {
    return false;
  }

}
