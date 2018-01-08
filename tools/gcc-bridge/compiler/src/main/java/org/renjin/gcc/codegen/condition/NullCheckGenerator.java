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
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;


public class NullCheckGenerator implements ConditionGenerator {

  private final GimpleOp op;
  private final PtrExpr ptrExpr;

  public NullCheckGenerator(GimpleOp op, PtrExpr ptrExpr) {
    this.op = op;
    this.ptrExpr = ptrExpr;
  }

  @Override
  public final void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    switch (op) {
      case EQ_EXPR:
        // "If ptrExpr is  NULL, then the condition is TRUE"
        ptrExpr.jumpIfNull(mv, trueLabel);
        mv.goTo(falseLabel);
        break;
      case NE_EXPR:
        // "If ptrExpr is NOT NULL, then the condition is TRUE"
        ptrExpr.jumpIfNull(mv, falseLabel);
        mv.goTo(trueLabel);
        break;
      
      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }
}
