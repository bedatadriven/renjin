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

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValueFunction;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.asm.Type;

import static org.renjin.gcc.codegen.expr.Expressions.*;

/**
 * Record value expression, backed by a JVM primitive array 
 */
public final class RecordArrayExpr implements RecordExpr {


  private RecordArrayValueFunction valueFunction;
  private JExpr array;
  private JExpr offset;
  private int arrayLength;

  public RecordArrayExpr(RecordArrayValueFunction valueFunction, JExpr array, JExpr offset, int arrayLength) {
    this.valueFunction = valueFunction;
    this.array = array;
    this.offset = offset;
    this.arrayLength = arrayLength;
  }

  public RecordArrayExpr(RecordArrayValueFunction valueFunction, JExpr array, int arrayLength) {
    this(valueFunction, array, zero(), arrayLength);
  }
  
  @Override
  public GExpr addressOf() {
    return new FatPtrPair(valueFunction, array, offset);
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
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
    return this;
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordUnitPtr toRecordUnitPtrExpr(RecordLayout layout) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    if(valueFunction.getValueType().equals(valueFunction.getValueType())) {
      return new FatPtrPair(valueFunction, array, offset);
    }
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
  public void store(MethodGenerator mv, GExpr rhs) {
    // GCC Return Value Optimization sometimes omits the valueOf operator in the gimple output
    // handle the case of a pointer being passed
    if(rhs instanceof FatPtrPair) {
      FatPtrPair fatPtrExpr = (FatPtrPair) rhs;
      mv.arrayCopy(fatPtrExpr.getArray(), fatPtrExpr.getOffset(), array, offset, constantInt(arrayLength));
    } else if (rhs instanceof RecordArrayExpr) {
      RecordArrayExpr arrayRhs = (RecordArrayExpr) rhs;
      mv.arrayCopy(arrayRhs.getArray(), arrayRhs.getOffset(), array, offset, constantInt(arrayLength));
    } else {
      throw new InternalCompilerException("Cannot assign " + rhs + " to " + this);
    }
  }

  public JExpr getArray() {
    return array;
  }

  public JExpr getOffset() {
    return offset;
  }

  public JExpr copyArray() {
    return copyOfArrayRange(array, offset, sum(offset, arrayLength));
  }

  @Override
  public GExpr memberOf(MethodGenerator mv, int fieldOffsetBits, int size, GimpleType memberType) {

    // All the fields in this record are necessarily primitives, so we need
    // simple to retrieve the element from within the array that corresponds to
    // the given field name
    JExpr fieldOffset = constantInt(fieldOffsetBits / 8 / valueFunction.getArrayElementBytes());
    JExpr offset = sum(this.offset, fieldOffset);

    // Because this value is backed by an array, we can also make it addressable.
    FatPtrPair address = new FatPtrPair(valueFunction, array, offset);

    // The members of this record may be either primitives, or arrays of primitives,
    // and it actually doesn't matter to us.

    Type fieldType = valueFunction.getValueType();

    if(memberType instanceof GimplePrimitiveType) {
      GimplePrimitiveType expectedType = (GimplePrimitiveType) memberType;

      // Return a single primitive value
      if(expectedType.jvmType().equals(fieldType)) {
        JExpr value = elementAt(array, offset);
        return new PrimitiveValue(expectedType, value, address);

      } else {
        throw new UnsupportedOperationException("TODO: " + fieldType + " -> " + expectedType);
      }

    } else if(memberType instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) memberType;
      GimplePrimitiveType componentType = (GimplePrimitiveType) arrayType.getComponentType();

      return new FatArrayExpr(arrayType,
          new PrimitiveValueFunction(componentType), arrayType.getElementCount(), array, offset);


    } else if(memberType instanceof GimpleRecordType) {
      return new RecordArrayExpr(valueFunction, array, fieldOffset,
          memberType.sizeOf() / valueFunction.getArrayElementBytes());


    } else {
      // Return an array that starts at this point
      return new FatPtrPair(valueFunction, address, array, offset);
    }
  }
}
