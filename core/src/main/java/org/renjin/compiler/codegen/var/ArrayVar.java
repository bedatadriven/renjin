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
package org.renjin.compiler.codegen.var;

import org.renjin.compiler.cfg.LivenessCalculator;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.ArrayExpr;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

/**
 * Store an S-expression known to be an atomic vector as a java array local variable.
 * the type of the vector must be known
 */
public class ArrayVar extends AbstractMutableVar {


  private final VectorType vectorType;
  private final int index;


  public ArrayVar(LValue variable, LivenessCalculator livenessCalculator, LocalVarAllocator localVars, ValueBounds bounds) {
    super(variable, livenessCalculator);
    this.index = localVars.reserveArray();
    this.vectorType = VectorType.of(bounds.getTypeSet());
  }

  @Override
  public CompiledSexp getCompiledExpr() {
    return new ArrayExpr(vectorType) {
      @Override
      public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
        if(!vectorType.getJvmArrayType().equals(ArrayVar.this.vectorType.getJvmArrayType())) {
          throw new UnsupportedOperationException("TODO: Array type mismatch");
        }
        mv.visitVarInsn(Opcodes.ALOAD, index);
      }
    };
  }

  @Override
  public void store(EmitContext emitContext, InstructionAdapter mv, CompiledSexp compiledSexp) {
    compiledSexp.loadArray(emitContext, mv, vectorType);
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }

}
