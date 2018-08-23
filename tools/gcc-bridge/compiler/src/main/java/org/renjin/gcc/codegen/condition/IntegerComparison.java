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
package org.renjin.gcc.codegen.condition;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;

import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Compares two signed 32-bit integers
 */
public class IntegerComparison implements ConditionGenerator {

  private final GimpleOp op;
  private final JExpr x;
  private final JExpr y;

  public IntegerComparison(GimpleOp op, JExpr x, JExpr y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    x.load(mv);
    y.load(mv);

    switch (op) {
      case LT_EXPR:
        mv.visitJumpInsn(IF_ICMPLT, trueLabel);
        break;

      case LE_EXPR:
        mv.visitJumpInsn(IF_ICMPLE, trueLabel);
        break;

      case EQ_EXPR:
        mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
        break;

      case NE_EXPR:
        mv.visitJumpInsn(IF_ICMPNE, trueLabel);
        break;

      case GT_EXPR:
        mv.visitJumpInsn(IF_ICMPGT, trueLabel);
        break;

      case GE_EXPR:
        mv.visitJumpInsn(IF_ICMPGE, trueLabel);
        break;
    }

    mv.goTo(falseLabel);
  }
}
