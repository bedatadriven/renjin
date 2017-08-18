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
package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.unit.RefConditionGenerator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

/**
 * Strategy for function pointer types
 */
public class FunPtrStrategy implements PointerTypeStrategy<FunPtr>, SimpleTypeStrategy<FunPtr> {
  
  public static final Type METHOD_HANDLE_TYPE = Type.getType(MethodHandle.class);

  public FunPtrStrategy() {
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RefPtrParamStrategy<>(this);
  }

  @Override
  public FunPtr variable(GimpleVarDecl decl, VarAllocator allocator) {
    return new FunPtr(allocator.reserve(decl.getNameIfPresent(), Type.getType(MethodHandle.class)));
  }

  @Override
  public FunPtr providedGlobalVariable(GimpleVarDecl decl, Field javaField) {
    if(javaField.getType().equals(MethodHandle.class)) {
      return new FunPtr(Expressions.staticField(javaField));
    }
    throw new InternalCompilerException("Cannot map global variable " + decl + " to " + javaField + ". " +
        "Field of type " + MethodHandle.class.getName() + " is required");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new FunPtrField(className, fieldName);
  }

  @Override
  public FunPtr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(METHOD_HANDLE_TYPE, fieldName, getValueFunction());
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(this);
  }

  @Override
  public ValueFunction getValueFunction() {
    return new FunPtrValueFunction(4);
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(new FunPtrValueFunction(4), 2);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, new FunPtrValueFunction(4));
  }

  @Override
  public FunPtr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    return value.toFunPtr();
  }

  @Override
  public FunPtr nullPointer() {
    return FunPtr.NULL_PTR;
  }

  @Override
  public ConditionGenerator comparePointers(MethodGenerator mv, GimpleOp op, FunPtr x, FunPtr y) {
    return new RefConditionGenerator(op, x.unwrap(), y.unwrap());
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, FunPtr p1, FunPtr p2, JExpr n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, FunPtr destination, FunPtr source, JExpr length, boolean buffer) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memorySet(MethodGenerator mv, FunPtr pointer, JExpr byteValue, JExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FunPtr unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer) {
    return new FunPtr(Expressions.cast(voidPointer, METHOD_HANDLE_TYPE));
  }

  @Override
  public FunPtr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    throw new UnsupportedOperationException("Cannot malloc function pointers");
  }

  @Override
  public FunPtr realloc(MethodGenerator mv, FunPtr pointer, JExpr newSizeInBytes) {
    throw new InternalCompilerException("Cannot realloc function pointers");
  }

  @Override
  public FunPtr pointerPlus(MethodGenerator mv, FunPtr pointer, JExpr offsetInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public String toString() {
    return "FunPtrStrategy";
  }

  @Override
  public Type getJvmType() {
    return METHOD_HANDLE_TYPE;
  }

  @Override
  public FunPtr wrap(JExpr expr) {
    Preconditions.checkArgument(expr.getType().equals(METHOD_HANDLE_TYPE));
    return new FunPtr(expr);
  }
}
