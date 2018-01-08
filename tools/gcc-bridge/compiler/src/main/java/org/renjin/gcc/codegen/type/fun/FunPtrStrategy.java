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
package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.lang.invoke.MethodHandle;

/**
 * Strategy for function pointer types
 */
public class FunPtrStrategy implements PointerTypeStrategy<FunPtrExpr> {
  
  public static final Type METHOD_HANDLE_TYPE = Type.getType(MethodHandle.class);

  public FunPtrStrategy() {
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new FunPtrParamStrategy();
  }

  @Override
  public FunPtrExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      JLValue unitArray = allocator.reserveUnitArray(decl.getNameIfPresent(), Type.getType(MethodHandle.class), Optional.<JExpr>absent());
      FatPtrPair address = new FatPtrPair(new FunPtrValueFunction(4), unitArray);
      JExpr value = Expressions.elementAt(address.getArray(), 0);
      return new FunPtrExpr(value, address);
    } else {
      return new FunPtrExpr(allocator.reserve(decl.getNameIfPresent(), Type.getType(MethodHandle.class)));
    }
  }

  @Override
  public FunPtrExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    if(expr.getType().equals(METHOD_HANDLE_TYPE)) {
      return new FunPtrExpr(expr);
    }
    throw new InternalCompilerException("Cannot map global variable " + decl + " to " + expr + ". " +
        "Field of type " + MethodHandle.class.getName() + " is required");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new FunPtrField(className, fieldName);
  }

  @Override
  public FunPtrExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(METHOD_HANDLE_TYPE, fieldName, getValueFunction());
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new FunPtrReturnStrategy();
  }

  @Override
  public ValueFunction getValueFunction() {
    return new FunPtrValueFunction(4);
  }

  @Override
  public VPtrStrategy pointerTo() {
    return new VPtrStrategy(new GimpleFunctionType().pointerTo());
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, new FunPtrValueFunction(4));
  }

  @Override
  public FunPtrExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    return value.toFunPtr();
  }

  @Override
  public FunPtrExpr nullPointer() {
    return FunPtrExpr.NULL_PTR;
  }

  @Override
  public FunPtrExpr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    throw new UnsupportedOperationException("Cannot malloc function pointers");
  }

  @Override
  public String toString() {
    return "FunPtrStrategy";
  }

}
