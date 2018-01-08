/**
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
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.Realloc;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Invokes a type-specific realloc() method to enlarge the new array
 */
public class FatPtrRealloc implements JExpr {

  private FatPtrPair pointer;
  private JExpr newLength;
  private Type arrayType;

  public FatPtrRealloc(FatPtrPair pointer, JExpr newLength) {
    this.pointer = pointer;
    this.newLength = newLength;
    arrayType = pointer.getArray().getType();
  }

  @Nonnull
  @Override
  public Type getType() {
    return arrayType;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {

    pointer.getArray().load(mv);
    pointer.getOffset().load(mv);
    newLength.load(mv);
    
    mv.invokestatic(Realloc.class, "realloc", Type.getMethodDescriptor(arrayType, arrayType, Type.INT_TYPE, Type.INT_TYPE));
  }
}
