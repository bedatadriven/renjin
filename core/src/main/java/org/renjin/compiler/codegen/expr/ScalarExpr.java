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

import org.renjin.compiler.codegen.ConstantBytecode;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.primitives.Vectors;
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
      } else if(this.type == VectorType.DOUBLE) {
        convertDoubleTo(mv, vectorType);
      } else {
        throw new UnsupportedOperationException("TODO: " + this.type + "=>" + vectorType);
      }
    }
  }

  private void convertIntTo(InstructionAdapter mv, VectorType loadType) {
    switch (loadType) {
      case LOGICAL:
      case INT:
      case BYTE:
        break;
      case DOUBLE:
        mv.invokestatic(Type.getInternalName(Vectors.class), "toDouble", "(I)D", false);
        break;
      default:
        throw new UnsupportedOperationException("TODO: " + this.type + "=>" + loadType);
    }
  }

  private void convertDoubleTo(InstructionAdapter mv, VectorType loadType) {
    switch (loadType) {
      case LOGICAL:
      case INT:
        mv.invokestatic(Type.getInternalName(Vectors.class), "toInt", "(D)I", false);
        break;
      case BYTE:
        mv.visitInsn(Opcodes.I2D);
        break;
      default:
        throw new UnsupportedOperationException("TODO:" + loadType);
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
    mv.visitInsn(Opcodes.ICONST_1);
    mv.newarray(vectorType.getJvmType());
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ICONST_0);
    loadScalar(context, mv, vectorType);

    switch (vectorType) {
      case BYTE:
        mv.visitInsn(Opcodes.BASTORE);
        break;
      case LOGICAL:
      case INT:
        mv.visitInsn(Opcodes.IASTORE);
        break;
      case DOUBLE:
        mv.visitInsn(Opcodes.DASTORE);
        break;
      case STRING:
        mv.visitInsn(Opcodes.AASTORE);
        break;
      default:
        throw new UnsupportedOperationException("todo: " + vectorType);
    }
  }

  @Override
  public CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr) {
    throw new UnsupportedOperationException("TODO");
  }
}
