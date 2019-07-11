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

import org.renjin.compiler.CompiledLoopBody;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;


public class ReadLoopIt implements Expression {

  public static final ValueBounds COUNTER_BOUNDS = ValueBounds.builder()
      .setTypeSet(TypeSet.INT)
      .addFlags(ValueBounds.LENGTH_ONE | ValueBounds.FLAG_NO_NA)
      .build();

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    return COUNTER_BOUNDS;
  }

  @Override
  public ValueBounds getValueBounds() {
    return COUNTER_BOUNDS;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new ScalarExpr(VectorType.INT) {
      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv) {
        mv.visitVarInsn(Opcodes.ILOAD, CompiledLoopBody.LOOP_ITERATION_INDEX);
      }
    };
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException("no children");
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException("no children");
  }

  @Override
  public String toString() {
    return "currentLoopIteration()";
  }
}
