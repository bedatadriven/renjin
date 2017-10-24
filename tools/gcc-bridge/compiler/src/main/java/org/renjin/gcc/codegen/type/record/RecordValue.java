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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.GSimpleExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

import static org.renjin.gcc.codegen.expr.Expressions.elementAt;


public class RecordValue implements GSimpleExpr, RecordExpr {
  
  private final JExpr ref;
  private final GExpr address;
  private RecordLayout layout;

  public RecordValue(RecordLayout layout, JExpr ref) {
    this.layout = layout;
    this.ref = ref;
    this.address = null;
  }

  public RecordValue(RecordLayout layout, JExpr ref, GExpr address) {
    this.layout = layout;
    this.ref = ref;
    this.address = address;
  }

  public Type getJvmType() {
    return ref.getType();
  }
  
  public JExpr getRef() {
    return ref;
  }


  @Override
  public void store(MethodGenerator mv, GExpr rhs) {

    JExpr rhsRef;
    if(rhs instanceof RecordValue) {
      rhsRef = ((RecordValue) rhs).unwrap();
    } else if(rhs instanceof ProvidedPtrExpr) {
      rhsRef = ((ProvidedPtrExpr) rhs).unwrap();
    } else if(rhs instanceof FatPtrPair) {
      FatPtrPair fatPtrExpr = (FatPtrPair) rhs;
      rhsRef =  Expressions.cast(elementAt(fatPtrExpr.getArray(), fatPtrExpr.getOffset()), getJvmType());
    } else {
      throw new InternalCompilerException("Cannot assign " + rhs + " to " + this);
    }

    ref.load(mv);
    Expressions.cast(rhsRef, ref.getType()).load(mv);
    
    mv.invokevirtual(ref.getType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, ref.getType()), false);
  }

  @Override
  public GExpr addressOf() {
    if (address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
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
    throw new UnsupportedCastException();
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
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
  public JExpr unwrap() {
    return ref;
  }

  @Override
  public GExpr memberOf(MethodGenerator mv, int fieldOffsetBits, int size, GimpleType type) {
    return layout.memberOf(mv, this, fieldOffsetBits, size, type);
  }

  public RecordValue doClone(MethodGenerator mv) {
    LocalVarAllocator.LocalVar clone = mv.getLocalVarAllocator().reserve(getJvmType());
    ref.load(mv);
    mv.invokevirtual(layout.getType(), "clone", Type.getMethodDescriptor(layout.getType()), false);
    clone.store(mv);

    return new RecordValue(layout, clone);
  }
}
