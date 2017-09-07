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
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.complex.ComplexValue;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.CompareToCmpGenerator;
import org.renjin.gcc.codegen.type.primitive.ObjectEqualsCmpGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.record.RecordLayout;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import java.lang.invoke.MethodHandle;

public class VPtrExpr implements PtrExpr {

  private JExpr ref;
  private final GExpr address;

  public VPtrExpr(JExpr ref) {
    this.ref = ref;
    this.address = null;
  }

  public VPtrExpr(JExpr ptr, GExpr address) {
    this.ref = ptr;
    this.address = address;
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
    if(address == null) {
      throw new NotAddressableException();
    }
    return address;
  }

  @Override
  public FunPtr toFunPtr() throws UnsupportedCastException {
    JExpr funPtr = Expressions.methodCall(ref, Ptr.class, "toMethodHandle",
        Type.getMethodDescriptor(Type.getType(MethodHandle.class)));

    return new FunPtr(funPtr);
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
    throw new UnsupportedOperationException("TODO: " + layout.getType());
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    throw new UnsupportedCastException();
  }

  @Override
  public VPtrRecordExpr toVPtrRecord(GimpleRecordType recordType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VArrayExpr toVArray(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("TODO");
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
  public void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length) {

    ref.load(mv);
    byteValue.load(mv);
    length.load(mv);

    mv.invokeinterface(Ptr.class, "memset", Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr length, boolean buffer) {

    ref.load(mv);
    source.toVPtrExpr().getRef().load(mv);
    length.load(mv);

    mv.invokeinterface(Ptr.class, buffer ? "memmove" : "memcpy", Type.VOID_TYPE, Type.getType(Ptr.class), Type.INT_TYPE);
  }

  @Override
  public PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes) {
    JExpr jExpr = Expressions.methodCall(ref, Ptr.class, "realloc",
        Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE),
        newSizeInBytes);

    return new VPtrExpr(jExpr);
  }

  @Override
  public PtrExpr pointerPlus(MethodGenerator mv, JExpr offsetInBytes) {
    return plus(offsetInBytes);
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {

    if(expectedType instanceof GimpleArrayType) {
      return new VArrayExpr(((GimpleArrayType) expectedType), this);
    }

    if(expectedType instanceof GimpleRecordType) {
      return new VPtrRecordExpr(((GimpleRecordType) expectedType), this);
    }

    if(expectedType instanceof GimplePrimitiveType) {
      PointerType pointerType = PointerType.ofType(expectedType);
      DerefExpr derefExpr = new DerefExpr(ref, pointerType);
      GimplePrimitiveType primitiveType = (GimplePrimitiveType) expectedType;

      return new PrimitiveValue(primitiveType, derefExpr, this);
    }

    if(expectedType instanceof GimpleComplexType) {
      GimpleComplexType complexType = (GimpleComplexType) expectedType;
      PointerType pointerType = PointerType.ofType(complexType.getPartType());

      DerefExpr realExpr = new DerefExpr(ref, pointerType);
      DerefExpr imaginaryExpr = new DerefExpr(ref, Expressions.constantInt(complexType.getPartType().sizeOf()), pointerType);

      return new ComplexValue(this, realExpr, imaginaryExpr);
    }

    if(expectedType instanceof GimpleIndirectType) {
      DerefExpr derefExpr = new DerefExpr(ref, PointerType.POINTER);
      return new VPtrExpr(derefExpr, this);
    }

    throw new UnsupportedOperationException("type: " + expectedType);
  }

  @Override
  public ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer) {
    switch (op) {
      case EQ_EXPR:
      case NE_EXPR:
        return new ObjectEqualsCmpGenerator(op, getRef(), otherPointer.toVPtrExpr().getRef());

      default:
        return new CompareToCmpGenerator(op, getRef(), otherPointer.toVPtrExpr().getRef());
    }
  }

  public GExpr valueOf(GimpleType expectedType, JExpr offset) {
    return plus(offset).valueOf(expectedType);
  }

  public VPtrExpr plus(JExpr offsetInBytes) {
    String plusMethod = Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE);
    JExpr plusExpr = Expressions.methodCall(
        ref, Ptr.class, "pointerPlus", plusMethod, offsetInBytes);

    return new VPtrExpr(plusExpr);
  }
}
