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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.BitFieldExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.record.RecordExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

/**
 * A record expression backed by a VPtr.
 */
public class VPtrRecordExpr implements RecordExpr {

  private GimpleRecordType recordType;
  private VPtrExpr pointer;

  public VPtrRecordExpr(GimpleRecordType recordType, VPtrExpr pointer) {
    this.recordType = recordType;
    this.pointer = pointer;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {


    pointer.getRef().load(mv);                              // destination

    // Because of some C++ black magic related to copy constructors, we can end up
    // with an assignment of a pointer to a value. Deal gracefully.

    if(rhs instanceof PtrExpr) {
      rhs.toVPtrExpr().getRef().load(mv);
    } else {
      rhs.toVPtrRecord(recordType).getRef().load(mv);
    }

    mv.iconst(recordType.sizeOf());                         // byte count

    mv.invokeinterface(Ptr.class, "memcpy", Type.VOID_TYPE, Type.getType(Ptr.class), Type.INT_TYPE);
  }

  @Override
  public GExpr addressOf() {
    return pointer;
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
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrRecordExpr toVPtrRecord(GimpleRecordType recordType) {
    return new VPtrRecordExpr(recordType, pointer);
  }

  @Override
  public VArrayExpr toVArray(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("TODO");
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
  public GExpr memberOf(MethodGenerator mv, int fieldOffsetBits, int size, GimpleType type) {

    if(fieldOffsetBits % 8 == 0) {
      return pointer.valueOf(type, Expressions.constantInt(fieldOffsetBits / 8));
    }

    // Handle bit fields

    if( type.equals(GimpleIntegerType.signed(8)) ||
        type.equals(GimpleIntegerType.unsigned(8))) {

      DerefExpr byteValue = new DerefExpr(
          pointer.getRef(),
          Expressions.constantInt(fieldOffsetBits / 8),
          PointerType.BYTE);

      BitFieldExpr bitFieldExpr = new BitFieldExpr(byteValue, fieldOffsetBits % 8, size);

      return new PrimitiveValue((GimplePrimitiveType) type, bitFieldExpr);
    }

    throw new UnsupportedOperationException("TODO: bitfield, expectedType = " + type);
  }

  public JExpr getRef() {
    return pointer.getRef();
  }
}
