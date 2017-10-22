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
package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ArrayExpr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

/**
 * An Gimple Array expression backed by a JVM array and an offset.
 */
public class FatArrayExpr implements ArrayExpr {

  private GimpleArrayType arrayType;
  private JExpr array;
  private JExpr offset;
  private ValueFunction valueFunction;
  private int length;
  
  public FatArrayExpr(GimpleArrayType arrayType, ValueFunction valueFunction, int length, JExpr array) {
    this.arrayType = arrayType;
    this.array = array;
    this.valueFunction = valueFunction;
    this.length = length;
    this.offset = Expressions.zero();
  }

  public FatArrayExpr(GimpleArrayType arrayType, ValueFunction valueFunction, int length, JExpr array, JExpr offset) {
    this.arrayType = arrayType;
    this.valueFunction = valueFunction;
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  public ValueFunction getValueFunction() {
    return valueFunction;
  }

  public JExpr getArray() {
    return array;
  }

  public JExpr getOffset() {
    return offset;
  }
  
  public GExpr first() {
    return valueFunction.dereference(array, offset);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    FatArrayExpr rhsExpr = (FatArrayExpr) rhs;
    int copyLength = Math.min(rhsExpr.length, length);
    
    valueFunction.memoryCopy(mv, 
        array, offset, 
        rhsExpr.getArray(), rhsExpr.getOffset(), 
        Expressions.constantInt(copyLength));
  }

  @Override
  public GExpr addressOf() {
    return new FatPtrPair(valueFunction, array, offset);
  }

  @Override
  public FunPtr toFunPtr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    return this;
  }

  @Override
  public PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException {
    return first().toPrimitiveExpr(targetType);
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
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
    return new FatPtrPair(valueFunction, array, offset);
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
  public GExpr elementAt(GimpleType expectedType, JExpr index) {
    // New offset  = ptr.offset + (index * value.length)
    // for arrays of doubles, for example, this will be the same as ptr.offset + index
    // but for arrays of complex numbers, this will be ptr.offset + (index * 2)
    JExpr newOffset = Expressions.sum(
        offset,
        Expressions.product(
            Expressions.difference(index, arrayType.getLbound()),
            valueFunction.getElementLength()));

    return valueFunction.dereference(array, newOffset);
  }
}
