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
import org.renjin.gcc.codegen.array.ArrayExpr;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

public class VPtrExpr implements PtrExpr {

  private JExpr ref;

  public VPtrExpr(JExpr ref) {
    this.ref = ref;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) this.ref).store(mv, rhs.toVPtrExpr().getRef());
  }

  public JExpr getRef() {
    return ref;
  }

  @Override
  public GExpr addressOf() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FunPtr toFunPtr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException {

    GimpleIntegerType pointerType = new GimpleIntegerType(32);
    pointerType.setUnsigned(true);

    return new PrimitiveValue(pointerType,
        Expressions.methodCall(ref, Ptr.class, "toInt", Type.getMethodDescriptor(Type.INT_TYPE)))
        .toPrimitiveExpr(targetType);
  }

  @Override
  public VoidPtr toVoidPtrExpr() throws UnsupportedCastException {
    return new VoidPtr(ref);
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrExpr toVPtrExpr() {
    return this;
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    PointerType pointerType = PointerType.ofType(expectedType);
    DerefExpr jvmExpr = new DerefExpr(ref, pointerType);

    switch (pointerType.getKind()) {
      case INTEGRAL:
      case FLOAT:
        return new PrimitiveValue(((GimplePrimitiveType) expectedType), jvmExpr);
      case POINTER:
        return new VPtrExpr(jvmExpr);
    }
    throw new UnsupportedOperationException("kind: " + pointerType.getKind());
  }
}
