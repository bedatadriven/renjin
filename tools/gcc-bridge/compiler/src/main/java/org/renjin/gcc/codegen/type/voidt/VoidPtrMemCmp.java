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
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.runtime.VoidPtr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Compares two pointers of unknown type by delegating to 
 * {@link ObjectPtr#memcmp(Object, Object, int)}
 */
public class VoidPtrMemCmp implements JExpr {
  
  private JExpr x;
  private JExpr y;
  private JExpr n;

  public VoidPtrMemCmp(JExpr x, JExpr y, JExpr n) {
    this.x = x;
    this.y = y;
    this.n = n;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    x.load(mv);
    y.load(mv);
    n.load(mv);
    mv.invokestatic(VoidPtr.class, "memcmp", 
        Type.getMethodDescriptor(Type.INT_TYPE, 
            Type.getType(Object.class), Type.getType(Object.class), Type.INT_TYPE));
  }
}
