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
package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Realloc a void pointer
 */
public class VoidPtrRealloc implements JExpr {
  
  private final JExpr pointer;
  private final JExpr newSizeInBytes;

  public VoidPtrRealloc(JExpr pointer, JExpr newSizeInBytes) {
    this.pointer = pointer;
    this.newSizeInBytes = newSizeInBytes;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.getType(Object.class);
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    // We can really only meaningfully allocate Fat Pointers, so cast the 
    // Object pointer to a Ptr and invoke realloc
    
    JExpr ptr = Expressions.cast(pointer, Type.getType(Ptr.class));
    ptr.load(mv);
    
    newSizeInBytes.load(mv);
    
    mv.invokeinterface(Ptr.class, "realloc", Type.getType(Ptr.class), Type.INT_TYPE);
  }
}
