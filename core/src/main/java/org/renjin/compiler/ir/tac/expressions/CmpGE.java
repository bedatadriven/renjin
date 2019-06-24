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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ConditionalExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import static org.renjin.repackaged.asm.Opcodes.IF_ICMPGE;

/**
 * Checks whether op1 is greater than or equal to op2. 
 * Op1 and op2 must be integers. (Not sexps!)
 */
public class CmpGE extends SpecializedCallExpression {

  private static final ValueBounds BOUNDS = ValueBounds.builder()
      .setTypeSet(TypeSet.LOGICAL)
      .addFlags(ValueBounds.LENGTH_ONE | ValueBounds.FLAG_NO_NA)
      .build();

  public CmpGE(Expression op1, Expression op2) {
    super(op1, op2);
  }

  @Override
  public String toString() {
    return arguments[0] + " >= " + arguments[1];
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    return getValueBounds();
  }

  @Override
  public ValueBounds getValueBounds() {
    return BOUNDS;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new ConditionalExpr() {
      @Override
      public void jumpIfTrue(EmitContext emitContext, InstructionAdapter mv, Label trueLabel) {
        childAt(0).getCompiledExpr(emitContext).loadScalar(emitContext, mv, VectorType.INT);
        childAt(1).getCompiledExpr(emitContext).loadScalar(emitContext, mv, VectorType.INT);
        mv.visitJumpInsn(IF_ICMPGE, trueLabel);
      }
    };
  }
}
