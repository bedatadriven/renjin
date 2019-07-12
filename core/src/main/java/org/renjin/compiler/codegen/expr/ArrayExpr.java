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
package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.eval.Support;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.LogicalArrayVector;

import java.util.Optional;

public abstract class ArrayExpr implements CompiledSexp {

  private final VectorType vectorType;

  public ArrayExpr(VectorType vectorType) {
    this.vectorType = vectorType;
  }

  @Override
  public void loadScalar(EmitContext context, InstructionAdapter mv, VectorType vectorType) {

    // Array
    loadArray(context, mv, vectorType);

    // Array index to load
    mv.visitInsn(Opcodes.ICONST_0);

    loadElement(mv);
  }

  public VectorType getVectorType() {
    return vectorType;
  }

  /**
   * Writes the instructions to load an element from the array. The array reference and the index
   * must already be on the stack.
   *
   */
  public void loadElement(InstructionAdapter mv) {
    switch (vectorType) {
      case BYTE:
        mv.visitInsn(Opcodes.BALOAD);
        break;
      case LOGICAL:
      case INT:
        mv.visitInsn(Opcodes.IALOAD);
        break;
      case DOUBLE:
        mv.visitInsn(Opcodes.DALOAD);
        break;
      case STRING:
        mv.visitInsn(Opcodes.AALOAD);
        break;
      default:
        throw new UnsupportedOperationException(vectorType.toString());
    }
  }

  @Override
  public void loadSexp(EmitContext context, InstructionAdapter mv) {
    loadArray(context, mv, vectorType);
    switch (vectorType) {
      case INT:
        mv.invokestatic(Type.getInternalName(IntArrayVector.class), "unsafe",
            Type.getMethodDescriptor(Type.getType(IntArrayVector.class), vectorType.getJvmArrayType()),
            false);
        break;
      case LOGICAL:
        mv.invokestatic(Type.getInternalName(LogicalArrayVector.class), "unsafe",
            Type.getMethodDescriptor(Type.getType(LogicalArrayVector.class), vectorType.getJvmArrayType()),
            false);
        break;
      case DOUBLE:
        mv.invokestatic(Type.getInternalName(DoubleArrayVector.class), "unsafe",
            Type.getMethodDescriptor(Type.getType(DoubleArrayVector.class), vectorType.getJvmArrayType()),
            false);
        break;

      default:
        throw new UnsupportedOperationException("TODO: " + vectorType);
    }
  }

  @Override
  public void loadLength(EmitContext context, InstructionAdapter mv) {
    loadArray(context, mv, vectorType);
    mv.visitInsn(Opcodes.ARRAYLENGTH);
  }

  @Override
  public void jumpIf(EmitContext emitContext, InstructionAdapter mv, Label trueLabel, Optional<Label> naLabel) {
    loadArray(emitContext, mv, vectorType);
    mv.invokestatic(Type.getInternalName(Support.class), "toBranchValue",
        Type.getMethodDescriptor(Type.INT_TYPE, vectorType.getJvmArrayType()), false);
  }

  @Override
  public CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr) {
    return new ArrayElementAt(this, indexExpr);
  }
}
