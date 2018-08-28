/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;

import static org.renjin.repackaged.asm.Opcodes.GOTO;
import static org.renjin.repackaged.asm.Opcodes.IFEQ;

public interface CompiledSexp {

  void loadSexp(EmitContext context, InstructionAdapter mv);

  void loadScalar(EmitContext context, InstructionAdapter mv, VectorType type);

  void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType);

  void loadLength(EmitContext context, InstructionAdapter mv);

  CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr);

  default void loadAsArgument(EmitContext emitContext, InstructionAdapter mv, Class argumentClass) {
    if(SEXP.class.isAssignableFrom(argumentClass)) {
      loadSexp(emitContext, mv);
    } else if(argumentClass.equals(int.class)) {
      loadScalar(emitContext, mv, VectorType.INT);
    } else if(argumentClass.equals(double.class)) {
      loadScalar(emitContext, mv, VectorType.DOUBLE);
    } else if(argumentClass.equals(String.class)) {
      loadScalar(emitContext, mv, VectorType.STRING);
    }
  }

  default void branch(EmitContext emitContext, InstructionAdapter mv, IRLabel trueTarget, IRLabel falseTarget) {
    loadScalar(emitContext, mv, VectorType.LOGICAL);
    mv.visitJumpInsn(IFEQ, emitContext.getAsmLabel(falseTarget));
    mv.visitJumpInsn(GOTO, emitContext.getAsmLabel(trueTarget));
  }

}
