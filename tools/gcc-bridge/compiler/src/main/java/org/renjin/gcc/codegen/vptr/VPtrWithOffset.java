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
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.record.RecordLayout;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Label;


/**
 * Vptr local variabler, which is stored as vptr + offset
 */
public class VPtrWithOffset implements PtrExpr {

  private VPtrExpr pointer;
  private JExpr offset;

  public VPtrWithOffset(JExpr pointer, JExpr offset) {
    this.pointer = new VPtrExpr(pointer);
    this.offset = offset;
  }

  public VPtrWithOffset(VPtrExpr pointer, JExpr offset) {
    this.pointer = pointer;
    this.offset = offset;
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    toVPtrExpr().jumpIfNull(mv, label);
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n) {
    return toVPtrExpr().memoryCompare(mv, otherPointer, n);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length) {
    toVPtrExpr().memorySet(mv, byteValue, length);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr length, boolean buffer) {
    toVPtrExpr().memoryCopy(mv, source, length, buffer);
  }

  @Override
  public PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes) {
    return toVPtrExpr().realloc(mv, newSizeInBytes);
  }

  @Override
  public PtrExpr pointerPlus(MethodGenerator mv, JExpr offsetInBytes) {
    return new VPtrWithOffset(this.pointer, Expressions.sum(offset, offsetInBytes));
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    return pointer.valueOf(expectedType, offset);
  }

  @Override
  public ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer) {
    return toVPtrExpr().comparePointer(mv, op, otherPointer);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {

    if(rhs instanceof VPtrWithOffset) {
      VPtrWithOffset rhsOffset = (VPtrWithOffset) rhs;
      pointer.store(mv, rhsOffset.pointer);
      ((JLValue) offset).store(mv, rhsOffset.offset);
    } else {
      pointer.store(mv, rhs.toVPtrExpr());
      ((JLValue) offset).store(mv, Expressions.zero());
    }
  }

  @Override
  public GExpr addressOf() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FunPtr toFunPtr() throws UnsupportedCastException {
    return toVPtrExpr().toFunPtr();
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    return toVPtrExpr().toArrayExpr();
  }

  @Override
  public PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException {
    return toVPtrExpr().toPrimitiveExpr(targetType);
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    return toVPtrExpr().toVoidPtrExpr();
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
    return toVoidPtrExpr().toRecordArrayExpr();
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    return pointer.plus(offset);
  }

  @Override
  public RecordUnitPtrExpr toRecordUnitPtrExpr(RecordLayout layout) {
    return toVPtrExpr().toRecordUnitPtrExpr(layout);
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    return toVPtrExpr().toFatPtrExpr(valueFunction);
  }

  @Override
  public VPtrRecordExpr toVPtrRecord(GimpleRecordType recordType) {
    return toVPtrExpr().toVPtrRecord(recordType);
  }

  @Override
  public VArrayExpr toVArray(GimpleArrayType arrayType) {
    return toVPtrExpr().toVArray(arrayType);
  }
}
