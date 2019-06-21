/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.complex.ComplexExpr;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

public class PtrCarryingExpr implements NumericIntExpr {

  private NumericIntExpr primitive;
  private final JExpr pointerExpr;

  public PtrCarryingExpr(NumericIntExpr primitive, JExpr pointerExpr) {
    this.primitive = primitive;
    this.pointerExpr = pointerExpr;
  }

  public JExpr getPointerExpr() {
    return pointerExpr;
  }

  @Override
  public GimplePrimitiveType getType() {
    return primitive.getType();
  }

  @Override
  public IntExpr toIntExpr() {
    return this;
  }

  @Override
  public PtrCarryingExpr plus(GExpr operand) {
    return new PtrCarryingExpr(primitive.plus(operand), pointerExpr);
  }

  @Override
  public PtrCarryingExpr minus(GExpr operand) {
    return new PtrCarryingExpr(primitive.minus(operand), pointerExpr);
  }

  @Override
  public PtrCarryingExpr multiply(GExpr operand) {
    return new PtrCarryingExpr(primitive.multiply(operand), pointerExpr);
  }

  @Override
  public NumericExpr divide(MethodGenerator mv, GExpr operand) {
    // Relationship to pointer is lost
    return primitive.divide(mv, operand);
  }

  @Override
  public NumericExpr remainder(GExpr operand) {
    // Relationship to pointer is lost
    return primitive.remainder(operand);
  }

  @Override
  public NumericExpr negative() {
    // Relationship to pointer is lost
    return primitive.negative();
  }

  @Override
  public NumericExpr min(GExpr operand) {
    // Relationship to pointer is lost
    // Though technically this could be implemented to return the
    // pointer with the lowest offset
    return primitive.min(operand);
  }

  @Override
  public NumericExpr max(GExpr operand) {
    // Relationship to pointer is lost
    // Though technically this could be implemented to return the
    // pointer with the highest offset
    return primitive.max(operand);
  }

  @Override
  public NumericIntExpr absoluteValue() {
    return new PtrCarryingExpr(primitive.absoluteValue(), pointerExpr);
  }

  @Override
  public ComplexExpr toComplexExpr() {
    // Relationship to pointer is lost
    return primitive.toComplexExpr();
  }

  @Override
  public RealExpr toRealExpr() {
    // Relationship to pointer is lost
    return primitive.toRealExpr();
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    // Relationship to pointer is lost
    return primitive.toBooleanExpr();
  }

  @Override
  public JExpr jexpr() {
    return primitive.jexpr();
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr generator) {
    return primitive.compareTo(op, generator);
  }

  @Override
  public PtrExpr addressOfReadOnly() {
    return primitive.addressOfReadOnly();
  }

  @Override
  public NumericIntExpr bitwiseXor(GExpr operand) {
    return new PtrCarryingExpr(primitive.bitwiseXor(operand), pointerExpr);
  }

  @Override
  public NumericIntExpr bitwiseNot() {
    // Relationship to pointer is lost
    return primitive.bitwiseNot();
  }

  @Override
  public NumericIntExpr bitwiseAnd(GExpr operand) {
    return new PtrCarryingExpr(primitive.bitwiseAnd(operand), pointerExpr);
  }

  @Override
  public NumericIntExpr bitwiseOr(GExpr operand) {
    return new PtrCarryingExpr(primitive.bitwiseOr(operand), pointerExpr);
  }

  @Override
  public GExpr shiftLeft(GExpr operand) {
    // Relationship to pointer is lost
    return primitive.shiftLeft(operand);
  }

  @Override
  public GExpr shiftRight(GExpr operand) {
    // Relationship to pointer is lost
    return primitive.shiftRight(operand);
  }

  @Override
  public GExpr rotateLeft(GExpr operand) {
    // Relationship to pointer is lost
    return primitive.rotateLeft(operand);
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    if(precision < 32) {
      // Relationship to pointer is lost
      return primitive.toSignedInt(precision);
    } else {
      return new PtrCarryingExpr((NumericIntExpr)primitive.toSignedInt(precision), pointerExpr);
    }
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    if(precision < 32) {
      // Relationship to pointer is lost
      return primitive.toSignedInt(precision);
    } else {
      return new PtrCarryingExpr((NumericIntExpr)primitive.toSignedInt(precision), pointerExpr);
    }
  }

  @Override
  public RealExpr toReal(int precision) {
    // Relationship to pointer is lost
    return primitive.toReal(precision);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {

    PrimitiveExpr primitiveRhs = rhs.toPrimitiveExpr();
    if(primitiveRhs instanceof PtrCarryingExpr) {
      // Store the integer _along with_ the associated pointer so
      // that once the fiddling with the offset is done, we can go back to a pointer.
      PtrCarryingExpr ptrCarryingRhs = (PtrCarryingExpr) primitiveRhs;
      primitive.store(mv, ptrCarryingRhs.primitive);
      ((JLValue)pointerExpr).store(mv, ptrCarryingRhs.pointerExpr);

    } else {
      // This is "just" a primitive, so reset the associated pointer to null
      primitive.store(mv, primitiveRhs);
      ((JLValue) pointerExpr).store(mv, Expressions.nullRef(Type.getType(Ptr.class)));
    }
  }

  @Override
  public PtrExpr addressOf() {
    // Relationship to pointer is lost
    return primitive.addressOf();
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
    return this;
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    return new VPtrExpr(Expressions.methodCall(pointerExpr, Ptr.class, "withOffset",
        Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE), primitive.toUnsignedInt(32).jexpr()));
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
    return this;
  }
}
