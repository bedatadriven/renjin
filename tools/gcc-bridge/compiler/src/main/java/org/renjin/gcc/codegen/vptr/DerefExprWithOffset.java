/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.gcc.codegen.vptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

public class DerefExprWithOffset implements JLValue {

  private PointerType pointerType;
  private JExpr pointer;
  private JExpr offsetBytes;

  public DerefExprWithOffset(PointerType pointerType, JExpr pointer, JExpr offsetBytes) {
    this.pointerType = pointerType;
    this.pointer = pointer;
    this.offsetBytes = offsetBytes;
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
    expr.load(mv);
    offsetBytes.load(mv);
    mv.invokeinterface(Type.getInternalName(Ptr.class), "set" + pointerType.titleCasedName(),
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, pointerType.getJvmType()));
  }
}
