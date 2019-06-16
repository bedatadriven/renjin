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
package org.renjin.gcc.codegen.vptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.ResourceWriter;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.runtime.MixedPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

/**
 * Represents a record backed by a byte array.
 */
public class VPtrRecordTypeStrategy extends RecordTypeStrategy<VPtrRecordExpr> {


  public VPtrRecordTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    super(recordTypeDef);
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new VPtrRecordParamStrategy(getRecordType());
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VPtrRecordReturnStrategy(getRecordType());
  }

  @Override
  public ValueFunction getValueFunction() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrRecordExpr variable(GimpleVarDecl decl, VarAllocator allocator) {

    // Allocate an array of bytes to store here
    JExpr malloc = Expressions.staticMethodCall(MixedPtr.class, "malloc",
        Type.getMethodDescriptor(Type.getType(MixedPtr.class), Type.INT_TYPE),
        Expressions.constantInt(getRecordType().sizeOf()));

    JLValue pointer = allocator.reserve(decl.getNameIfPresent(), Type.getType(Ptr.class), malloc);

    return new VPtrRecordExpr(recordType, new VPtrExpr(pointer));
  }

  @Override
  public VPtrRecordExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    assert expr.getType().equals(Type.getType(Ptr.class));
    return new VPtrRecordExpr(((GimpleRecordType) decl.getType()), new VPtrExpr(expr));
  }

  @Override
  public VPtrRecordExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, ResourceWriter resourceWriter, GimpleConstructor value) {
    // Create a temporary variable for this constructed record array.
    Type pointerType = Type.getType(MixedPtr.class);
    VPtrExpr malloc = VPtrStrategy.malloc(pointerType, Expressions.constantInt(this.getRecordType().sizeOf()));
    VPtrExpr tempVar = new VPtrExpr(mv.getLocalVarAllocator().reserve(pointerType));
    tempVar.store(mv, malloc);

    int offset = 0;
    for (GimpleConstructor.Element fieldCtor : value.getElements()) {
      GimpleFieldRef field = (GimpleFieldRef) fieldCtor.getField();
      GExpr fieldExpr = exprFactory.findGenerator(fieldCtor.getValue(), field.getType());

      tempVar.valueOf(field.getType(), Expressions.constantInt(offset + field.getOffsetBytes()))
            .store(mv, fieldExpr);
    }

    return new VPtrRecordExpr(recordType, tempVar);
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VPtrStrategy(getGimpleType());
  }

  @Override
  public VArrayStrategy arrayOf(GimpleArrayType arrayType) {
    return new VArrayStrategy(arrayType);
  }

  @Override
  public VPtrRecordExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    return value.toVPtrRecord(this.getRecordType());
  }
}
