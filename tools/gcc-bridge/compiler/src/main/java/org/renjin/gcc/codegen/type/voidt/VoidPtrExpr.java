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
package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveExpr;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.VoidPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;


public class VoidPtrExpr implements RefPtrExpr {
  
  private JExpr objectRef;
  private FatPtr address;

  public VoidPtrExpr(JExpr objectRef, FatPtr address) {
    this.objectRef = objectRef;
    this.address = address;
  }

  public VoidPtrExpr(JExpr objectRef) {
    this.objectRef = objectRef;
    this.address = null;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    JLValue lhs = (JLValue) this.objectRef;
    lhs.store(mv, rhs.toVoidPtrExpr().jexpr());
  }
  
  @Override
  public PtrExpr addressOf() {
    if(address == null) {
      throw new NotAddressableException();
    }
    return address;
  }

  @Override
  public FunPtrExpr toFunPtr() {
    return new FunPtrExpr(Expressions.cast(objectRef, Type.getType(MethodHandle.class)));
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PrimitiveExpr toPrimitiveExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public JExpr jexpr() {
    return objectRef;
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    objectRef.load(mv);
    mv.ifnull(label);
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n) {
    return new VoidPtrMemCmp(jexpr(), otherPointer.toVoidPtrExpr().jexpr(), n);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length) {
    objectRef.load(mv);
    byteValue.load(mv);
    length.load(mv);

    mv.invokestatic(org.renjin.gcc.runtime.VoidPtr.class, "memset",
        Type.getMethodDescriptor(Type.VOID_TYPE,
            Type.getType(Object.class),
            Type.INT_TYPE,
            Type.INT_TYPE));
  }

  @Override
  public void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr length, boolean buffer) {

    jexpr().load(mv);
    source.toVoidPtrExpr().jexpr().load(mv);
    length.load(mv);

    mv.invokestatic(org.renjin.gcc.runtime.VoidPtr.class, "memcpy",
          Type.getMethodDescriptor(Type.VOID_TYPE,
              Type.getType(Object.class), Type.getType(Object.class), Type.INT_TYPE));
  }

  @Override
  public PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes) {
    return new VoidPtrExpr(new VoidPtrRealloc(jexpr(), newSizeInBytes));
  }

  @Override
  public PtrExpr pointerPlus(MethodGenerator mv, final JExpr offsetInBytes) {
    // We have to rely on run-time support for this because we don't know
    // what kind of pointer is stored here
    return new VoidPtrExpr(new JExpr() {

      @Nonnull
      @Override
      public Type getType() {
        return Type.getType(Object.class);
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        objectRef.load(mv);
        offsetInBytes.load(mv);
        mv.invokestatic(org.renjin.gcc.runtime.VoidPtr.class, "pointerPlus",
            Type.getMethodDescriptor(Type.getType(Object.class),
                Type.getType(Object.class), Type.INT_TYPE));
      }
    });
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    throw new UnsupportedOperationException("void pointers cannot be dereferenced.");
  }

  @Override
  public ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer) {
    return new VoidPtrComparison(op, jexpr(), otherPointer.toVoidPtrExpr().jexpr());
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    return this;
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    return new VPtrExpr(Expressions.staticMethodCall(VoidPtr.class, "toPtr",
        Type.getMethodDescriptor(Type.getType(Ptr.class), Type.getType(Object.class)), objectRef));
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    return new ProvidedPtrExpr(Expressions.cast(jexpr(), jvmType));
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    JExpr wrapperInstance = Wrappers.cast(valueFunction.getValueType(), objectRef);
    JExpr arrayField = Wrappers.arrayField(wrapperInstance);
    JExpr offsetField = Wrappers.offsetField(wrapperInstance);

    return new FatPtrPair(valueFunction, arrayField, offsetField);
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

}
