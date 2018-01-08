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
package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.primitive.PrimitiveExpr;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.record.ReferenceConditions;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.FunctionPtr1;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import java.lang.invoke.MethodHandle;


public class FunPtrExpr implements RefPtrExpr {

  public static final FunPtrExpr NULL_PTR = new FunPtrExpr();

  private JExpr methodHandleExpr;
  private FatPtr address;

  private FunPtrExpr() {
    this.methodHandleExpr = Expressions.nullRef(Type.getType(MethodHandle.class));
  }

  public FunPtrExpr(JExpr methodHandleExpr) {
    this.methodHandleExpr = methodHandleExpr;
    this.address = null;
  }

  public FunPtrExpr(JExpr methodHandleExpr, FatPtr address) {
    this.methodHandleExpr = methodHandleExpr;
    this.address = address;
  }

  public JExpr jexpr() {
    return methodHandleExpr;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) methodHandleExpr).store(mv, rhs.toFunPtr().methodHandleExpr);
  }

  @Override
  public PtrExpr addressOf() {
    if(address == null) {
      throw new InternalCompilerException("Not addressable");
    }
    return address;
  }

  @Override
  public FunPtrExpr toFunPtr() {
    return this;
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public PrimitiveExpr toPrimitiveExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    return new VoidPtrExpr(methodHandleExpr, address);
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    return new VPtrExpr(Expressions.staticMethodCall(FunctionPtr1.class, "malloc",
        Type.getMethodDescriptor(Type.getType(Ptr.class), Type.getType(MethodHandle.class)), this.methodHandleExpr),
          address);
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    throw new UnsupportedOperationException("TODO");
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
    methodHandleExpr.load(mv);
    mv.ifnull(label);
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr length, boolean buffer) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PtrExpr pointerPlus(MethodGenerator mv, JExpr offsetInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    return this;
  }

  @Override
  public ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer) {
    return ReferenceConditions.compare(op, jexpr(), otherPointer.toFunPtr().jexpr());
  }
}
