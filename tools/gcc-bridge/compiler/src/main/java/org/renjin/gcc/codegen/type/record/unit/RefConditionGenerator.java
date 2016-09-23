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
package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveCmpGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;

import static org.renjin.gcc.codegen.expr.Expressions.identityHash;

/**
 * Compares two JVM reference expressions.
 * 
 * <p>For EQ_EXPR and NE_EXPR, we use standard {@code IF_ACMPEQ} and {@code IF_ACMPNE} bytecode instructions.</p>
 * 
 * <p>For LT_EXPR and GT_EXPR, we compare the result of {@link System#identityHashCode(Object)}</p>
 * 
 */
public class RefConditionGenerator implements ConditionGenerator {
  private final GimpleOp op;
  private final JExpr x;
  private final JExpr y;

  public RefConditionGenerator(GimpleOp op, JExpr x, JExpr y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    
    switch (op) {
      case EQ_EXPR:
        x.load(mv);
        y.load(mv);
        mv.visitJumpInsn(Opcodes.IF_ACMPEQ, trueLabel);
        mv.visitJumpInsn(Opcodes.GOTO, falseLabel);
        break;
      case NE_EXPR:
        x.load(mv);
        y.load(mv);
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, trueLabel);
        mv.visitJumpInsn(Opcodes.GOTO, falseLabel);
        break;

      case LT_EXPR:
      case GT_EXPR:
        PrimitiveCmpGenerator hashCmpGenerator = new PrimitiveCmpGenerator(op, identityHash(x), identityHash(y));
        hashCmpGenerator.emitJump(mv, trueLabel, falseLabel);
        break;

      default:  
        throw new UnsupportedOperationException("Unsupported pointer comparison: " + op);
    }
  }
}
