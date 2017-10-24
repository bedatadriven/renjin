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
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.complex.ComplexValue;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.CompareToCmpGenerator;
import org.renjin.gcc.codegen.type.primitive.ObjectEqualsCmpGenerator;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.lang.invoke.MethodHandle;

public class VPtrExpr implements PtrExpr {

  /**
   * The reference to the base instance of {@link Ptr}.
   */
  private JExpr baseRef;

  /**
   * This pointer's address.
   */
  private final GExpr address;

  /**
   * An expression containing an additional offset, in bytes, relative to {@code baseRef}.
   */
  private final Optional<JExpr> offset;

  public VPtrExpr(JExpr ref) {
    this.baseRef = ref;
    this.address = null;
    this.offset = Optional.absent();
  }

  public VPtrExpr(JExpr baseRef, JExpr offset) {
    this.baseRef = baseRef;
    this.offset = Optional.of(offset);
    this.address = null;
  }

  public VPtrExpr(JExpr ptr, GExpr address) {
    this.baseRef = ptr;
    this.address = address;
    this.offset = Optional.absent();
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    VPtrExpr rhsVPtr = rhs.toVPtrExpr();
    if(offset.isPresent()) {
      ((JLValue) this.baseRef).store(mv, rhsVPtr.baseRef);
      ((JLValue) offset.get()).store(mv, rhsVPtr.getOffset());
    } else {
      ((JLValue) this.baseRef).store(mv, rhsVPtr.getRef());
    }
  }

  public JExpr getBaseRef() {
    return baseRef;
  }

  /**
   *
   * Returns a reference to a single {@code Ptr}. If this {@code VPtrExpr} has a non-zero offset,
   * {@link Ptr#pointerPlus(int)} is invoked at runtime to create a new instance that incorporates this offset.
   */
  public JExpr getRef() {
    if(offset.isPresent() && !offset.get().equals(Expressions.zero())) {
      String plusMethod = Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE);
      return Expressions.methodCall(
          baseRef, Ptr.class, "pointerPlus", plusMethod, getOffset());

    } else {
      return baseRef;
    }
  }

  /**
   *
   * @return the offset in bytes relative to the {@code baseRef}
   */
  public JExpr getOffset() {
    return offset.or(Expressions.zero());
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
    JExpr funPtr = Expressions.methodCall(getRef(), Ptr.class, "toMethodHandle",
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
        Expressions.methodCall(getRef(), Ptr.class, "toInt", Type.getMethodDescriptor(Type.INT_TYPE)))
        .toPrimitiveExpr(targetType);
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    return new VoidPtrExpr(getRef());
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
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    // The only way we can cast back is if this is a RecordUnitPtr. Let's try.
    JExpr arrayObject = Expressions.methodCall(getRef(), Ptr.class, "getArray",
          Type.getMethodDescriptor(Type.getType(Object.class)));

    JExpr recordUnitPtr = Expressions.cast(arrayObject, jvmType);

    return new ProvidedPtrExpr(recordUnitPtr);
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
  public NumericExpr toNumericExpr() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    getRef().load(mv);
    mv.invokeinterface(Ptr.class, "isNull", Type.BOOLEAN_TYPE);
    mv.ifne(label);
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n) {
    return Expressions.methodCall(getRef(), Ptr.class, "memcmp",
        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(Ptr.class), Type.INT_TYPE),
        otherPointer.toVPtrExpr().getRef(),
        n);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length) {

    getRef().load(mv);
    byteValue.load(mv);
    length.load(mv);

    mv.invokeinterface(Ptr.class, "memset", Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr length, boolean buffer) {

    getRef().load(mv);
    source.toVPtrExpr().getRef().load(mv);
    length.load(mv);

    mv.invokeinterface(Ptr.class, buffer ? "memmove" : "memcpy", Type.VOID_TYPE, Type.getType(Ptr.class), Type.INT_TYPE);
  }

  @Override
  public PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes) {
    JExpr jExpr = Expressions.methodCall(getRef(), Ptr.class, "realloc",
        Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE),
        newSizeInBytes);

    return new VPtrExpr(jExpr);
  }

  @Override
  public PtrExpr pointerPlus(MethodGenerator mv, JExpr offsetInBytes) {
    return pointerPlus(offsetInBytes);
  }

  private PtrExpr pointerPlus(JExpr offsetInBytes) {
    return new VPtrExpr(this.baseRef, Expressions.sum(getOffset(), offsetInBytes));
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
      DerefExpr derefExpr = new DerefExpr(baseRef, getOffset(), pointerType);
      GimplePrimitiveType primitiveType = (GimplePrimitiveType) expectedType;

      return new PrimitiveValue(primitiveType, derefExpr, this);
    }

    if(expectedType instanceof GimpleComplexType) {
      GimpleComplexType complexType = (GimpleComplexType) expectedType;
      PointerType pointerType = PointerType.ofType(complexType.getPartType());

      JExpr realOffset = getOffset();
      JExpr complexOffset = Expressions.sum(realOffset, complexType.getPartType().sizeOf());

      DerefExpr realExpr = new DerefExpr(baseRef, realOffset, pointerType);
      DerefExpr imaginaryExpr = new DerefExpr(baseRef, complexOffset, pointerType);

      return new ComplexValue(this, realExpr, imaginaryExpr);
    }

    if(expectedType instanceof GimpleIndirectType) {
      DerefExpr derefExpr = new DerefExpr(baseRef, getOffset(), PointerType.POINTER);
      return new VPtrExpr(derefExpr, this);
    }

    throw new UnsupportedOperationException("type: " + expectedType);
  }

  public GExpr valueOf(GimpleType expectedType, JExpr offset) {
    return pointerPlus(offset).valueOf(expectedType);
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
}
