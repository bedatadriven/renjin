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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveType;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.RecordUnitPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

public class ProvidedPtrExpr implements RefPtrExpr {

  private JExpr ref;
  private FatPtr address;

  public ProvidedPtrExpr(JExpr ref) {
    this.ref = ref;
  }

  public ProvidedPtrExpr(JExpr ref, FatPtr address) {
    this.ref = ref;
    this.address = address;
  }

  public Type getJvmType() {
    return ref.getType();
  }
  
  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) ref).store(mv, rhs.toProvidedPtrExpr(ref.getType()).jexpr());
  }

  @Override
  public PtrExpr addressOf() {
    if(address == null) {
      throw new NotAddressableException();
    }
    return address;
  }

  public JExpr jexpr() {
    return ref;
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    ref.load(mv);
    mv.ifnull(label);
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length) {
    jexpr().load(mv);
    byteValue.load(mv);
    length.load(mv);
    mv.invokevirtual(getJvmType(), "memset",
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE), false);
  }

  @Override
  public PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PtrExpr pointerPlus(MethodGenerator mv, final JExpr offsetInBytes) {
    // According to our analysis conducted before-hand, there should be no pointer
    // to a sequence of records of this type with more than one record, so the result should
    // be undefined.
    JExpr expr = new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return getJvmType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        Label zero = new Label();
        offsetInBytes.load(mv);
        mv.ifeq(zero);
        mv.anew(Type.getType(ArrayIndexOutOfBoundsException.class));
        mv.dup();
        mv.invokeconstructor(Type.getType(ArrayIndexOutOfBoundsException.class));
        mv.athrow();
        mv.mark(zero);
        ProvidedPtrExpr.this.ref.load(mv);
      }
    };

    return new ProvidedPtrExpr(expr);
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    throw new UnsupportedOperationException(
        String.format("Provided type '%s' is opaque and may not be dereferenced.", getJvmType()));
  }

  @Override
  public ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer) {
    return ReferenceConditions.compare(op, jexpr(), otherPointer.toProvidedPtrExpr(getJvmType()).jexpr());
  }

  @Override
  public FunPtrExpr toFunPtr() {
    return FunPtrExpr.NULL_PTR;
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PrimitiveExpr toPrimitiveExpr() throws UnsupportedCastException {
    return PrimitiveType.UINT32.fromStackValue(Expressions.identityHash(ref));
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    return new VoidPtrExpr(ref);
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    return new VPtrExpr(Expressions.newObject(RecordUnitPtr.class,
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), ref));
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    return new ProvidedPtrExpr(Expressions.cast(jexpr(), jvmType));
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
  public void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr length, boolean buffer) {
    ref.load(mv);
    source.toProvidedPtrExpr(getJvmType()).jexpr().load(mv);
    mv.invokevirtual(getJvmType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, getJvmType()), false);
  }
}
