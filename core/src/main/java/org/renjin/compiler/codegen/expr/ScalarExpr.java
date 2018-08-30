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

import org.renjin.compiler.codegen.ConstantBytecode;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AttributeMap;

public abstract class ScalarExpr implements CompiledSexp {

  private VectorType type;
  private final AttributeMap attributes;

  public ScalarExpr(VectorType type, AttributeMap attributes) {
    this.type = type;
    this.attributes = attributes;
  }

  public ScalarExpr(VectorType type) {
    this.type = type;
    this.attributes = null;
  }

  @Override
  public final void loadScalar(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
    loadScalar(context, mv);

    if(this.type != vectorType) {
      if(this.type == VectorType.INT || this.type == VectorType.LOGICAL) {
        convertIntTo(mv, vectorType);
      } else {
        throw new UnsupportedOperationException("TODO: " + this.type + "=>" + vectorType);
      }
    }
  }

  private void convertIntTo(InstructionAdapter mv, VectorType loadType) {
    switch (loadType) {
      case LOGICAL:
      case INT:
        break;
      case DOUBLE:
        mv.visitInsn(Opcodes.I2D);
        break;
      default:
        throw new UnsupportedOperationException("TODO: " + this.type + "=>" + loadType);
    }
  }

  public abstract void loadScalar(EmitContext context, InstructionAdapter mv);

  @Override
  public final void loadSexp(EmitContext context, InstructionAdapter mv) {
    loadScalar(context, mv, type);
    if(attributes == null || attributes == AttributeMap.EMPTY) {
      mv.invokestatic(type.getVectorClassType().getInternalName(), "valueOf",
          Type.getMethodDescriptor(type.getVectorClassType(), type.getJvmType()), false);
    } else {

      ConstantBytecode.pushAttributes(mv, attributes);
      mv.invokestatic(type.getVectorClassType().getInternalName(), "valueOf",
          Type.getMethodDescriptor(type.getVectorClassType(),
              type.getJvmType(),
              Type.getType(AttributeMap.class)), false);
    }
  }

  @Override
  public final void loadLength(EmitContext context, InstructionAdapter mv) {
    mv.visitInsn(Opcodes.ICONST_1);
  }


  @Override
  public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr) {
    throw new UnsupportedOperationException("TODO");
  }
}
