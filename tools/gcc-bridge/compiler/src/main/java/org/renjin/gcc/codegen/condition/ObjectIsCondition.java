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
package org.renjin.gcc.codegen.condition;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;

/**
 * Jumps to true if the two object references are the same (x==y)
 */
public class ObjectIsCondition implements ConditionGenerator {

  private JExpr x;
  private JExpr y;

  public ObjectIsCondition(JExpr x, JExpr y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    x.load(mv);
    y.load(mv);
    mv.visitJumpInsn(Opcodes.IF_ACMPEQ, trueLabel);
    mv.goTo(falseLabel);
  }

  public ConditionGenerator inverse() {
    return new InverseConditionGenerator(this);
  }
}
