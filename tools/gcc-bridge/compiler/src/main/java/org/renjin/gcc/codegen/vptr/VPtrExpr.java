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
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Label;

public class VPtrExpr implements PtrExpr {

  private JExpr ref;

  public VPtrExpr(JExpr ref) {
    this.ref = ref;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    if(rhs instanceof FatPtrPair) {
      storeFatPtr(mv, ((FatPtrPair) rhs));
    } else if(rhs instanceof VPtrExpr) {
      VPtrExpr pointerRhs = (VPtrExpr) rhs;
      storeRef(mv, pointerRhs.ref);
    } else {
      throw new UnsupportedOperationException("TODO:" + rhs.getClass().getSimpleName());
    }
  }

  public JExpr getRef() {
    return ref;
  }

  private void storeRef(MethodGenerator mv, JExpr ref) {
    ((JLValue) this.ref).store(mv, ref);
  }

  private void storeFatPtr(MethodGenerator mv, FatPtrPair rhs) {
    storeRef(mv, rhs.vpointer());
  }

  @Override
  public GExpr addressOf() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    if(expectedType instanceof GimplePrimitiveType) {
      return new PrimitiveValue(new DerefExpr(ref, PointerType.valueOf((GimplePrimitiveType) expectedType)));
    }
    throw new UnsupportedOperationException("TODO: " + expectedType);
  }
}
