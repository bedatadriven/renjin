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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.Memset;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.List;

/**
 * Translates a pointer array and offset to a Record value represented by a primitive array.
 */
public class RecordArrayValueFunction implements ValueFunction {

  
  private Type fieldType;
  private int arrayLength;
  private GimpleRecordType gimpleRecordType;

  public RecordArrayValueFunction(Type fieldType, int fieldCount, GimpleRecordType gimpleRecordType) {
    this.fieldType = fieldType;
    this.arrayLength = fieldCount;
    this.gimpleRecordType = gimpleRecordType;
  }

  @Override
  public Type getValueType() {
    return fieldType;
  }

  @Override
  public GimpleType getGimpleValueType() {
    return gimpleRecordType;
  }

  /**
   * Returns the number of array elements required for each record value. 
   * 
   * <p>For example, a {@code struct} in C defined as: </p>
   * <pre>
   * struct point {
   *   double x;
   *   double y;
   * }  
   * </pre>
   * <p>Would require 2 elements in a {@code double[]} for each {@code point} value.</p>
   */
  @Override
  public int getElementLength() {
    return arrayLength;
  }

  @Override
  public int getArrayElementBytes() {
    return GimplePrimitiveType.fromJvmType(fieldType).sizeOf();
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    // No constructor is required for individual fields;
    // they will default to zero.
    return Optional.absent();
  }

  @Override
  public GExpr dereference(final JExpr array, final JExpr offset) {
    return new RecordArrayExpr(this, array, offset, arrayLength);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return dereference(wrapperInstance.getArray(), wrapperInstance.getOffset());
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, JExpr destinationArray, JExpr destinationOffset, JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset,
        Expressions.product(valueCount, arrayLength));
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    Memset.primitiveMemset(mv, fieldType, array, offset, byteValue, length);
  }

  @Override
  public String toString() {
    return "RecordArray[" + fieldType + "]";
  }
}

