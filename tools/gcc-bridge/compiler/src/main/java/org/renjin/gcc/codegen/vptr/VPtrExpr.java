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
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.record.RecordLayout;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.gimple.type.*;
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
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
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
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    return new VoidPtrExpr(ref);
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
  public RecordUnitPtr toRecordUnitPtrExpr(RecordLayout layout) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    throw new UnsupportedCastException();
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    ref.load(mv);
    mv.invokeinterface(Ptr.class, "isNull", Type.BOOLEAN_TYPE);
    mv.ifne(label);
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n) {
    return Expressions.methodCall(ref, Ptr.class, "memcmp",
        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(Ptr.class), Type.INT_TYPE),
        ref,
        otherPointer.toVPtrExpr().getRef(),
        n);
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    return valueOf(expectedType, Expressions.constantInt(0));
  }

  public GExpr valueOf(GimpleType expectedType, JExpr offset) {

    if(expectedType instanceof GimpleArrayType) {
      return new VArrayExpr(((GimpleArrayType) expectedType), this);
    }

    if(expectedType instanceof GimpleRecordType) {
      return new VPtrRecordExpr(((GimpleRecordType) expectedType), plus(offset));
    }

    PointerType pointerType = PointerType.ofType(expectedType);
    DerefExpr derefExpr = new DerefExpr(ref, offset, pointerType);

    if(expectedType instanceof GimplePrimitiveType) {
      GimplePrimitiveType primitiveType = (GimplePrimitiveType) expectedType;

      return new PrimitiveValue(primitiveType, derefExpr);
    }

    if(expectedType instanceof GimpleIndirectType) {
      return new VPtrExpr(derefExpr);
    }

    throw new UnsupportedOperationException("type: " + expectedType);
  }

  public VPtrExpr plus(JExpr offsetInBytes) {
    String plusMethod = Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE);
    JExpr plusExpr = Expressions.methodCall(
        ref, Ptr.class, "pointerPlus", plusMethod, offsetInBytes);

    return new VPtrExpr(plusExpr);
  }
}
