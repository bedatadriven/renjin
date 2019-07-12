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

import org.renjin.compiler.codegen.ConstantBytecode;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.eval.Support;
import org.renjin.primitives.Vectors;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;

import java.util.Optional;

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
  public void jumpIf(EmitContext emitContext, InstructionAdapter mv, Label trueLabel, Optional<Label> naLabel) {

    switch (type) {

      case BYTE:
      case BOOLEAN:
        loadScalar(emitContext, mv, VectorType.BOOLEAN);
        mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
        break;


      case LOGICAL:
      case INT:
        if(naLabel.isPresent()) {
          intJumpIf(emitContext, mv, trueLabel, naLabel.get());
        } else {
          intJumpIf(emitContext, mv, trueLabel);
        }
        break;

      case DOUBLE:
        if(naLabel.isPresent()) {
          doubleJumpIf(emitContext, mv, trueLabel, naLabel.get());
        } else {
          doubleJumpIf(emitContext, mv, trueLabel);
        }
        break;

      case STRING:
        throw new IllegalStateException("Should not be here!");
    }
  }

  private void doubleJumpIf(EmitContext emitContext, InstructionAdapter mv, Label trueLabel, Label label) {
    throw new UnsupportedOperationException("TODO");
  }

  private void doubleJumpIf(EmitContext emitContext, InstructionAdapter mv, Label trueLabel) {
    loadScalar(emitContext, mv, VectorType.DOUBLE);
    mv.invokestatic(Type.getInternalName(Support.class), "test", "(D)Z", false);
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
  }

  protected void intJumpIf(EmitContext emitContext, InstructionAdapter mv, Label trueLabel) {
    loadScalar(emitContext, mv, type);
    mv.dup();
    mv.invokestatic(Type.getInternalName(Support.class), "checkNotNA", "(I)V", false);
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
  }

  private void intJumpIf(EmitContext emitContext, InstructionAdapter mv, Label trueLabel, Label naLabel) {
    loadScalar(emitContext, mv, type);
    mv.iconst(IntVector.NA);
    mv.visitJumpInsn(Opcodes.IF_ICMPEQ, naLabel);

    loadScalar(emitContext, mv, type);
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
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
