/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.runtime.VoidPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

/**
 * Compares two pointers.
 */
public class VoidPtrComparison implements ConditionGenerator {
  private final GimpleOp op;
  private final JExpr x;
  private final JExpr y;

  public VoidPtrComparison(GimpleOp op, JExpr x, JExpr y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    // x and y might actually be references to Fat Pointers,
    // which cannot be directly compared, as we actually have to unwrap
    // them to check to see whether they point to the same array.
    
    // To avoid too much complexity and type checking here, we delegate to a runtime function
    
    x.load(mv);
    y.load(mv);
    
    mv.invokestatic(VoidPtr.class, "compare", 
        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(Object.class), Type.getType(Object.class)));

    // The compare method leaves a result of -1, 0, +1 on the stack.
    switch (op) {
      case LT_EXPR:
        mv.iflt(trueLabel);
        break;
      case LE_EXPR:
        mv.ifle(trueLabel);
        break;
      case EQ_EXPR:
        mv.ifeq(trueLabel);
        break;
      case NE_EXPR:
        mv.ifne(trueLabel);
        break;
      case GT_EXPR:
        mv.ifgt(trueLabel);
        break;
      case GE_EXPR:
        mv.ifge(trueLabel);
        break;
    }
    mv.goTo(falseLabel);
  }
}
