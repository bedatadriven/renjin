/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen.vptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

class DerefExpr implements JLValue {

  private final JExpr pointer;
  private final JExpr offsetBytes;
  private final PointerType pointerType;

  public DerefExpr(JExpr pointer, JExpr offsetBytes, PointerType pointerType) {
    this.pointer = pointer;
    this.offsetBytes = offsetBytes;
    this.pointerType = pointerType;
  }

  @Nonnull
  @Override
  public Type getType() {
    return pointerType.getJvmType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    pointer.load(mv);

    if(isConstantEqualTo(offsetBytes, 0)) {
      mv.invokeinterface(Type.getInternalName(Ptr.class), "get" + pointerType.titleCasedName(),
          Type.getMethodDescriptor(pointerType.getJvmType()));
    } else {
      JExpr index = isAligned();
      if(index != null) {
        index.load(mv);
        mv.invokeinterface(Type.getInternalName(Ptr.class), "getAligned" + pointerType.titleCasedName(),
            Type.getMethodDescriptor(pointerType.getJvmType(), Type.INT_TYPE));
      } else {
        offsetBytes.load(mv);
        mv.invokeinterface(Type.getInternalName(Ptr.class), "get" + pointerType.titleCasedName(),
            Type.getMethodDescriptor(pointerType.getJvmType(), Type.INT_TYPE));
      }
    }
  }

  @Override
  public void store(MethodGenerator mv, JExpr expr) {
    pointer.load(mv);
    if(isConstantEqualTo(offsetBytes, 0)) {
      // store at offset zero
      expr.load(mv);
      mv.invokeinterface(Type.getInternalName(Ptr.class), "set" + pointerType.titleCasedName(),
          Type.getMethodDescriptor(Type.VOID_TYPE, pointerType.getJvmType()));

    } else {
      JExpr index = isAligned();
      if (index != null) {
        index.load(mv);
        expr.load(mv);
        mv.invokeinterface(Type.getInternalName(Ptr.class), "setAligned" + pointerType.titleCasedName(),
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, pointerType.getJvmType()));

      } else {
        offsetBytes.load(mv);
        expr.load(mv);
        mv.invokeinterface(Type.getInternalName(Ptr.class), "set" + pointerType.titleCasedName(),
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, pointerType.getJvmType()));
      }
    }
  }

  private JExpr isAligned() {
    // Byte and boolean accessors/setters do not have aligned versions.
    if(pointerType.getSize() < 2) {
      return null;
    }

    if(offsetBytes instanceof ConstantValue) {
      int constantOffsetBytes = ((ConstantValue) offsetBytes).getIntValue();
      if(constantOffsetBytes % pointerType.getSize() == 0) {
        return Expressions.constantInt(constantOffsetBytes / pointerType.getSize());
      }
    }

    if(offsetBytes instanceof BinaryOpExpr) {
      BinaryOpExpr op = (BinaryOpExpr) offsetBytes;
      if(op.getOpcode() == Opcodes.IMUL) {

        if(isConstantEqualTo(op.getX(), pointerType.getSize())) {
          return op.getY();
        }
        if(isConstantEqualTo(op.getY(), pointerType.getSize())) {
          return op.getX();
        }
      }
    }
    return null;
  }


  private boolean isConstantEqualTo(JExpr expr, int value) {
    return expr instanceof ConstantValue && ((ConstantValue) expr).getIntValue() == value;
  }

}
