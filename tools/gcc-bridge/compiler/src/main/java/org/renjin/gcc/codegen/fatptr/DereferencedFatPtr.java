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
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveExpr;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

public class DereferencedFatPtr implements RefPtrExpr, FatPtr {

  private final ValueFunction valueFunction;
  private JExpr array;
  private JExpr offset;
  private Type wrapperType;

  public DereferencedFatPtr(JExpr array, JExpr offset, ValueFunction valueFunction) {
    this.array = array;
    this.offset = offset;
    this.valueFunction = valueFunction;
    this.wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
  }

  private ArrayElement element() {
    return Expressions.elementAt(array, offset);
  }
  
  private JExpr castedElement() {
    return Expressions.cast(element(), wrapperType);
  }

  @Override
  public JExpr jexpr() {
    return element();
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {

    if(rhs instanceof FatPtr) {
      element().store(mv, ((FatPtr) rhs).wrap());

    } else {
      throw new UnsupportedOperationException("TODO: " + rhs.getClass().getName());
    }
  }

  @Override
  public GExpr addressOf() {
    return new FatPtrPair(valueFunction, array, offset);
  }

  @Override
  public FunPtrExpr toFunPtr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
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
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    return this;
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
  public Type getValueType() {
    return valueFunction.getValueType();
  }

  @Override
  public boolean isAddressable() {
    return false;
  }

  @Override
  public JExpr wrap() {
    return element();
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return Wrappers.toPair(mv, valueFunction, castedElement());
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    element().load(mv);
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
    return valueFunction.dereference(array, offset);
  }

  @Override
  public ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer) {
    throw new UnsupportedOperationException("TODO");
  }
}
