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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

class DerefExpr implements JLValue {

  private JExpr pointer;
  private JExpr offsetBytes;
  private PointerType pointerType;

  public DerefExpr(JExpr pointer, JExpr offsetBytes, PointerType pointerType) {
    this.pointer = pointer;
    this.offsetBytes = offsetBytes;
    this.pointerType = pointerType;
  }

  public DerefExpr(JExpr pointer, PointerType pointerType) {
    this(pointer, Expressions.zero(), pointerType);
  }


  @Nonnull
  @Override
  public Type getType() {
    return pointerType.getJvmType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    pointer.load(mv);
    offsetBytes.load(mv);
    mv.invokeinterface(Type.getInternalName(Ptr.class), "get" + pointerType.titleCasedName(),
        Type.getMethodDescriptor(pointerType.getJvmType(), Type.INT_TYPE));
  }

  @Override
  public void store(MethodGenerator mv, JExpr expr) {
    pointer.load(mv);
    offsetBytes.load(mv);
    expr.load(mv);
    mv.invokeinterface(Type.getInternalName(Ptr.class), "set" + pointerType.titleCasedName(),
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, pointerType.getJvmType()));
  }

}
