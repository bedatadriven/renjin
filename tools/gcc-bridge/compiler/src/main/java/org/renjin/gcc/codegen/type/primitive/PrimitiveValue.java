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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.op.CastGenerator;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;


public class PrimitiveValue implements GSimpleExpr {

  private GimplePrimitiveType primitiveType;
  private JExpr expr;
  private GExpr address;

  public PrimitiveValue(GimplePrimitiveType primitiveType, JExpr expr) {
    this.primitiveType = primitiveType;
    this.expr = expr;
  }

  public PrimitiveValue(GimplePrimitiveType primitiveType, JExpr expr, GExpr address) {
    this.primitiveType = primitiveType;
    this.expr = expr;
    this.address = address;
  }

  public JExpr getExpr() {
    return expr;
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }

  @Override
  public FunPtr toFunPtr() {
    return FunPtr.NULL_PTR;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    
    PrimitiveValue primitiveRhs = rhs.toPrimitiveExpr(primitiveType);
    
    ((JLValue) expr).store(mv, primitiveRhs.getExpr());
  }

  @Override
  public JExpr unwrap() {
    return expr;
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException {
    return new PrimitiveValue(targetType, CastGenerator.cast(unwrap(), primitiveType, targetType));
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
    return new VPtrExpr(Expressions.staticMethodCall(BytePtr.class, "of",
        Type.getMethodDescriptor(Type.getType(Ptr.class), Type.INT_TYPE),
        CastGenerator.cast(expr, primitiveType, GimpleIntegerType.unsigned(32))));
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    throw new UnsupportedOperationException("TODO");
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

}
