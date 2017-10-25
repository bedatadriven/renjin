/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.codegen.condition;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;

import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Jumps on the basis of a comparison result.
 */
public class Comparison implements ConditionGenerator {

  private final GimpleOp op;
  private final JExpr flag;

  public Comparison(GimpleOp op, JExpr flag) {
    this.op = op;
    this.flag = flag;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {

    flag.load(mv);

    switch (op) {
      case LT_EXPR:
        // 1: x < y
        mv.visitJumpInsn(IFLT, trueLabel);
        break;
      case LE_EXPR:
        // 1 : x < y
        // 0 : x == y
        mv.visitJumpInsn(IFLE, trueLabel);
        break;
      case EQ_EXPR:
        // 0 : x == y
        mv.visitJumpInsn(IFEQ, trueLabel);
        break;

      case NE_EXPR:
        mv.visitJumpInsn(IFNE, trueLabel);
        break;

      case UNGT_EXPR:
      case GT_EXPR:
        mv.visitJumpInsn(IFGT, trueLabel);
        break;

      case GE_EXPR:
        mv.visitJumpInsn(IFGE, trueLabel);
        break;

      default:
        throw new UnsupportedOperationException("op: " + op);
    }

    mv.goTo(falseLabel);
  }
}
