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
package org.renjin.gcc.codegen.type.record.unit;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordConstructor;
import org.renjin.gcc.codegen.type.record.RecordLayout;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.lang.reflect.Modifier;


public class RecordUnitPtrStrategy implements PointerTypeStrategy<RecordUnitPtrExpr>, SimpleTypeStrategy<RecordUnitPtrExpr> {
  
  private RecordClassTypeStrategy strategy;
  private RecordUnitPtrValueFunction valueFunction;
  
  public RecordUnitPtrStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
    this.valueFunction = new RecordUnitPtrValueFunction(strategy);
  }

  public boolean isEmpty() {
    return strategy.getJvmType().equals(Type.getType(Object.class));
  }

  @Override
  public ParamStrategy getParamStrategy() {
    if(strategy.getJvmType().equals(Type.getType(Object.class))) {
      return new EmptyRecordPtrParam(this);
    } else {
      return new RecordUnitPtrParam(this);
    }
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new RecordUnitPtrField(className, fieldName, strategy.getLayout());
  }

  @Override
  public RecordUnitPtrExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, valueFunction);
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(valueFunction, 2);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, valueFunction);
  }

  @Override
  public RecordUnitPtrExpr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    return value.toRecordUnitPtrExpr(strategy.getLayout());
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new RecordUnitPtrReturnStrategy(strategy.getLayout());
  }

  @Override
  public ValueFunction getValueFunction() {
    return valueFunction;
  }

  @Override
  public RecordUnitPtrExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {

      // Declare this as a Unit array so that we can get a FatPtrExpr if needed
      JExpr unitArray = allocator.reserveUnitArray(decl.getName(), strategy.getJvmType(), Optional.<JExpr>absent());

      FatPtrPair address = new FatPtrPair(valueFunction, unitArray);
      ArrayElement instance = Expressions.elementAt(unitArray, 0);
      
      return new RecordUnitPtrExpr(getLayout(), instance, address);
      
    } else {
      return new RecordUnitPtrExpr(getLayout(), allocator.reserve(decl.getNameIfPresent(), strategy.getJvmType()));
    }
  }

  @Override
  public RecordUnitPtrExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    Type javaFieldType = expr.getType();
    if(!javaFieldType.equals(this.strategy.getJvmType())) {
      throw new UnsupportedOperationException("Cannot map global variable " + decl + " to existing field " + expr + ". " +
          "Expected field of type " + this.strategy.getJvmType());
    }

    if(readOnly) {
      // If it's final, then we can make this variable addressable by creating an array on
      // demand. Changes to the pointer's value by C code will have no effect, but that's a good thing??
      FatPtrPair fakeAddress = new FatPtrPair(valueFunction, Expressions.newArray(expr));
      return new RecordUnitPtrExpr(getLayout(), expr, fakeAddress);
    }

    return new RecordUnitPtrExpr(getLayout(), expr);
  }

  @Override
  public RecordUnitPtrExpr malloc(MethodGenerator mv, JExpr sizeInBytes) {

    if (isUnitConstant(sizeInBytes)) {
      throw new InternalCompilerException(getClass().getSimpleName() + " does not support (T)malloc(size) where " +
          "size != sizeof(T). This is probably because of a mistake in the choice of strategy by the compiler.");
    }
    return new RecordUnitPtrExpr(getLayout(), new RecordConstructor(strategy));
  }


  @Override
  public RecordUnitPtrExpr nullPointer() {
    return new RecordUnitPtrExpr(getLayout(), Expressions.nullRef(strategy.getJvmType()));
  }


  private boolean isUnitConstant(JExpr length) {
    if(!(length instanceof ConstantValue)) {
      return false;
    }
    ConstantValue constantValue = (ConstantValue) length;
    return constantValue.getType().equals(Type.INT_TYPE) && constantValue.getIntValue() == 1;
  }

  public Type getJvmType() {
    return strategy.getJvmType();
  }

  @Override
  public RecordUnitPtrExpr wrap(JExpr expr) {
    return new RecordUnitPtrExpr(getLayout(), expr);
  }

  @Override
  public String toString() {
    return "RecordUnitPtrStrategy[" + strategy.getRecordTypeDef().getName() + "]";
  }

  public GimpleRecordType getGimpleType() {
    return strategy.getRecordType();
  }

  public RecordLayout getLayout() {
    return strategy.getLayout();
  }
}
