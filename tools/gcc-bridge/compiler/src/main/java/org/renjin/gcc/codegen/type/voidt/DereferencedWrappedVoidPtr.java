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
package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.repackaged.asm.Type;

public class DereferencedWrappedVoidPtr extends VoidPtr {

  private WrappedFatPtrExpr wrapperInstance;

  public DereferencedWrappedVoidPtr(WrappedFatPtrExpr wrapperInstance) {
    super(wrapperInstance.valueExpr(), wrapperInstance);
    this.wrapperInstance = wrapperInstance;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    if(rhs instanceof VoidPtr) {
      wrapperInstance.wrap().load(mv);
      ((VoidPtr) rhs).unwrap().load(mv);
      mv.invokevirtual(wrapperInstance.wrap().getType(), "set",
          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);
    
    } else {
      throw new UnsupportedOperationException("TODO: rhs = " + rhs.getClass().getName());
    }
    
  }
}
