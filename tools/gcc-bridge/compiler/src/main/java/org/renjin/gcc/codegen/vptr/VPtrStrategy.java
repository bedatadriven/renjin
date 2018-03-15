/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.runtime.MixedPtr;
import org.renjin.gcc.runtime.PointerPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import java.util.Optional;

/**
 * Implements a C pointer using the {@link org.renjin.gcc.runtime.Ptr} interface.
 */
public class VPtrStrategy implements PointerTypeStrategy {

  private GimpleType baseType;
  private PointerType pointerType;

  public VPtrStrategy(GimpleType baseType) {
    this.baseType = baseType;
    this.pointerType = PointerType.ofType(baseType);
  }

  @Override
  public VPtrExpr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    return malloc(baseType, sizeInBytes);
  }

  public static VPtrExpr malloc(GimpleType baseType, JExpr sizeInBytes) {

    Type implType = choosePtrImplType(baseType);

    return malloc(implType, sizeInBytes);
  }

  static VPtrExpr malloc(Type implType, JExpr sizeInBytes) {
    String mallocDescriptor = Type.getMethodDescriptor(implType, Type.INT_TYPE);
    JExpr pointer = Expressions.staticMethodCall(implType, "malloc", mallocDescriptor, sizeInBytes);

    return new VPtrExpr(pointer);
  }

  private static Type choosePtrImplType(GimpleType baseType) {
    if(baseType instanceof GimplePrimitiveType) {
      return PointerType.ofPrimitiveType(((GimplePrimitiveType) baseType)).alignedImpl();
    }
    if(baseType instanceof GimpleArrayType) {
      return choosePtrImplType(((GimpleArrayType) baseType).getComponentType());
    }
    if(baseType instanceof GimpleIndirectType) {
      return Type.getType(PointerPtr.class);
    }
    if(baseType instanceof GimpleRecordType) {
      return Type.getType(MixedPtr.class);
    }
    if(baseType instanceof GimpleVoidType) {
      return Type.getType(MixedPtr.class);
    }
    throw new UnsupportedOperationException("TODO: " + baseType);
  }

  @Override
  public GExpr nullPointer() {
    JExpr pointer = Expressions.staticField(pointerType.alignedImpl(), "NULL", pointerType.alignedImpl());

    return new VPtrExpr(pointer);
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new VPtrParamStrategy();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VPtrReturnStrategy();
  }

  @Override
  public ValueFunction getValueFunction() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      GimplePointerType pointerType = this.baseType.pointerTo();
      JLValue unitArray = allocator.reserveUnitArray(decl.getNameIfPresent(), Type.getType(Ptr.class), Optional.empty());
      VPtrValueFunction valueFunction = new VPtrValueFunction(pointerType);
      FatPtrPair address = new FatPtrPair(valueFunction, unitArray, Expressions.constantInt(0));
      return address.valueOf(pointerType);

    } else {
      // For "normal" local variables, allocate an extra "offset" variable so that
      // we don't need to create new Ptr instances for pointer arithmatic within the function body.

      JLValue pointer = allocator.reserve(decl.getNameIfPresent(), Type.getType(Ptr.class));
      JLValue offset = allocator.reserveOffsetInt(decl.getNameIfPresent());

      return new VPtrExpr(pointer, offset);
    }
  }

  @Override
  public GExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new VPtrFieldStrategy(className, fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VPtrStrategy(baseType.pointerTo().pointerTo());
  }

  @Override
  public VArrayStrategy arrayOf(GimpleArrayType arrayType) {
    return new VArrayStrategy(arrayType);
  }

  @Override
  public GExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    return value.toVPtrExpr();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof VPtrStrategy;
  }

  @Override
  public int hashCode() {
    return 1;
  }
}
