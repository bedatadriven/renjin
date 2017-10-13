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
package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;


/**
 * Strategy for handling pointers of unknown type.
 * 
 * <p>GCC Bridge compiles {@code void *} types as values of type {@code java.lang.Object}.
 * Void pointers may point a Fat Pointer object such as {@link org.renjin.gcc.runtime.DoublePtr}, 
 * to a {@link java.lang.invoke.MethodHandle}, or to record type for records that use the 
 * {@link org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy}.</p>
 */
public class VoidPtrStrategy implements PointerTypeStrategy<VoidPtrExpr>, SimpleTypeStrategy<VoidPtrExpr> {
  
  public static final Type OBJECT_TYPE = Type.getType(Object.class);

  public VoidPtrStrategy() {
  }

  @Override
  public VoidPtrExpr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    return new VoidPtrExpr(new NewMallocThunkExpr(sizeInBytes));
  }

  @Override
  public VoidPtrExpr nullPointer() {
    return new VoidPtrExpr(Expressions.nullRef(Type.getType(Object.class)));
  }


  @Override
  public ParamStrategy getParamStrategy() {
    return new VoidPtrParamStrategy();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VoidPtrReturnStrategy();
  }

  @Override
  public ValueFunction getValueFunction() {
    return new VoidPtrValueFunction();
  }

  @Override
  public VoidPtrExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      Type objectArrayType = Type.getType("[Ljava/lang/Object;");
      JLValue unitArray = allocator.reserve(decl.getNameIfPresent(), objectArrayType, Expressions.newArray(Object.class, 1));
      FatPtrPair address = new FatPtrPair(new VoidPtrValueFunction(), unitArray);
      JExpr value = Expressions.elementAt(unitArray, 0);
      
      return new VoidPtrExpr(value, address);

    } else {

      return new VoidPtrExpr(allocator.reserve(decl.getNameIfPresent(), Type.getType(Object.class)));
    }
  }

  @Override
  public VoidPtrExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    if(expr.getType().getSort() != Type.OBJECT) {
      throw new UnsupportedOperationException("Cannot map void* global pointer " + decl + " to primitive field " +
          expr + ". Must be an Object.");
    }
    return new VoidPtrExpr(expr);
  }

  @Override
  public VoidPtrExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new VoidPtrField(className, fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, new VoidPtrValueFunction());
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new FatPtrStrategy(new VoidPtrValueFunction(), 2);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, new VoidPtrValueFunction());
  }

  @Override
  public VoidPtrExpr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    return value.toVoidPtrExpr();
  }

  @Override
  public String toString() {
    return "VoidPtrStrategy";
  }

  @Override
  public Type getJvmType() {
    return Type.getType(Object.class);
  }

  @Override
  public VoidPtrExpr wrap(JExpr expr) {
    return new VoidPtrExpr(expr);
  }
}
