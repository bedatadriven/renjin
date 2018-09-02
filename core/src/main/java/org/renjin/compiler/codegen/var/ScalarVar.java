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
package org.renjin.compiler.codegen.var;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AttributeMap;

/**
 * Stores an SEXP using a single "scalar" local variable,
 * either a primitive or an object of type java.lang.String.
 */
public class ScalarVar extends VariableStrategy {

  private final VectorType type;
  private final int index;

  public ScalarVar(LocalVarAllocator localVars, ValueBounds bounds) {
    assert bounds.hasNoAttributes();
    type = VectorType.of(bounds.getTypeSet());
    index = localVars.reserve(type.getJvmType());
  }

  @Override
  public CompiledSexp getCompiledExpr() {
    return new ScalarExpr(type, AttributeMap.EMPTY) {

      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv) {
        switch (type) {
          case BYTE:
          case LOGICAL:
          case INT:
            mv.visitVarInsn(Opcodes.ILOAD, index);
            break;
          case DOUBLE:
            mv.visitVarInsn(Opcodes.DLOAD, index);
            break;
          case STRING:
            mv.visitVarInsn(Opcodes.ALOAD, index);
            break;
          default:
            throw new UnsupportedOperationException(type.toString());
        }
      }
    };
  }

  @Override
  public void store(EmitContext emitContext, InstructionAdapter mv, CompiledSexp compiledSexp) {
    compiledSexp.loadScalar(emitContext, mv, type);

    switch (type) {
      case BYTE:
      case LOGICAL:
      case INT:
        mv.visitVarInsn(Opcodes.ISTORE, index);
        break;
      case DOUBLE:
        mv.visitVarInsn(Opcodes.DSTORE, index);
        break;
      case STRING:
        mv.visitVarInsn(Opcodes.ASTORE, index);
        break;
      default:
        throw new UnsupportedOperationException(type.toString());
    }
  }
}
