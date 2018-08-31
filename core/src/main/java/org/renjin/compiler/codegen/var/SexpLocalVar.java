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

package org.renjin.compiler.codegen.var;

import org.renjin.compiler.cfg.LivenessCalculator;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;

/**
 * Stores an SEXP as a reference to a {@link org.renjin.sexp.SEXP} as
 * a JVM local variable
 */
public class SexpLocalVar extends AbstractMutableVar {

  public static final Type SEXP_TYPE = Type.getType(SEXP.class);
  private final int index;

  public SexpLocalVar(LValue variable, LivenessCalculator livenessCalculator, int varIndex) {
    super(variable, livenessCalculator);
    this.index = varIndex;
  }

  @Override
  public CompiledSexp getCompiledExpr() {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        mv.visitVarInsn(Opcodes.ALOAD, index);
      }
    };
  }

  @Override
  public void store(EmitContext emitContext, InstructionAdapter mv, CompiledSexp compiledSexp) {
    compiledSexp.loadSexp(emitContext, mv);
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }

}
