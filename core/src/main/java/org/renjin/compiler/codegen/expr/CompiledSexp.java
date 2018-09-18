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
package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;

public interface CompiledSexp {

  /**
   * Writes the bytecode to load this expression onto the stack as an {@code SEXP} object.
   */
  void loadSexp(EmitContext context, InstructionAdapter mv);

  /**
   * Writes the bytecode to load this expression onto the stack as a scalar of
   * the given {@code vectorType}. The value on the stack will match the type
   * of {@code type.jvmType()}
   */
  void loadScalar(EmitContext context, InstructionAdapter mv, VectorType vectorType);

  /**
   * Writes the bytecode to load this expression onto the stack as an array of
   * the given {@code vectorType}.
   *
   */
  void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType);

  /**
   * Writes the bytecode to load the length of this expression onto the stack as an {@code int}
   */
  void loadLength(EmitContext context, InstructionAdapter mv);

  /**
   * Writes the bytecode to load this expression onto the stack as a value matching the type
   * of {@code argumentClass}.
   *
   */
  default void loadAsArgument(EmitContext emitContext, InstructionAdapter mv, Class argumentClass) {
    if(SEXP.class.isAssignableFrom(argumentClass)) {
      loadSexp(emitContext, mv);
    } else if(argumentClass.equals(int.class)) {
      loadScalar(emitContext, mv, VectorType.INT);
    } else if(argumentClass.equals(boolean.class)) {
      loadScalar(emitContext, mv, VectorType.LOGICAL);
    } else if(argumentClass.equals(double.class)) {
      loadScalar(emitContext, mv, VectorType.DOUBLE);
    } else if(argumentClass.equals(String.class)) {
      loadScalar(emitContext, mv, VectorType.STRING);
    } else {
      throw new UnsupportedOperationException("TODO: " + argumentClass.getName());
    }
  }

  /**
   * Writes the bytecode to jump to the given {@code label} if this expression is true
   */
  default void jumpIfTrue(EmitContext emitContext, InstructionAdapter mv, Label trueLabel) {
    loadScalar(emitContext, mv, VectorType.INT);
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
  }

  /**
   * Returns a new {@code CompiledExpr} that has the value of an element of this
   * expression at the given {@code indexExpr}.
   */
  CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr);


  void loadAndPop(EmitContext emitContext, InstructionAdapter mv);
}
