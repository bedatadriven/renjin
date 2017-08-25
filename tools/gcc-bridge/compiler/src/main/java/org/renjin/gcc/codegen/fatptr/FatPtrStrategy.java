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
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.codegen.vptr.VPtrParamStrategy;
import org.renjin.gcc.codegen.vptr.VPtrReturnStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.repackaged.asm.Type;

import java.lang.reflect.Field;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;

/**
 * Strategy for pointer types that uses a combination of an array value and an offset value
 */
public class FatPtrStrategy implements PointerTypeStrategy<FatPtr> {

  private ValueFunction valueFunction;
  private boolean parametersWrapped = true;
  private int indirectionLevel;

  /**
   * The JVM type of the array used to back the pointer
   */
  private Type arrayType; 

  public FatPtrStrategy(ValueFunction valueFunction, int indirectionLevel) {
    assert indirectionLevel >= 1;
    this.valueFunction = valueFunction;
    this.indirectionLevel = indirectionLevel;
    if(indirectionLevel == 2) {
      this.arrayType = Type.getType("[Ljava/lang/Object;");
    } else {
      this.arrayType = Type.getType("[" + valueFunction.getValueType().getDescriptor());
    }
  }
  
  public boolean isParametersWrapped() {
    return parametersWrapped;
  }

  public ValueFunction getValueFunction() {
    return valueFunction;
  }

  public FatPtrStrategy setParametersWrapped(boolean parametersWrapped) {
    this.parametersWrapped = parametersWrapped;
    return this;
  }

  @Override
  public FatPtr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      // If this variable needs to be addressable, then we need to store it in a unit length pointer
      // so that we can later get its "address"
      // For example, if creating a double pointer variable that needs to be later addressed:
      
      // C:
      //
      // void init(double **pp) {
      //   double *p = malloc(3 * sizeof(double));
      //   p[1] = 42.0;
      //   p[2] = 33.4;
      //   *pp = p+1;
      // }      
      // 
      // void test() {
      //   double *p;   <--- Needs to be addressable
      //   init(&p)
      //   double x = *p + *(p+1)
      // }

      
      // The solution is to store the pointer as a unit-length array of wrappers. Then we can pass this 
      // to other methods and allow them to set the array and offset
      
      // void init(ObjectPtr pp) {
      //   double p[] = new double[3];
      //   int p$offset = 0;
      //   p[p$offset + 1] = 42.0;
      //   p[p$offset + 2] = 33.4;
      //   pp.array[pp.offset] = new DoublePtr(p, p$offset)
      // }      
      // 
      // void test() {
      //   DoublePtr[] p = new DoublePtr[] { new DoublePtr() };
      //   init(new ObjectPtr(p, 0));
      //   double x = p.array[p.offset] + p.array[p.offset+1]
      // }

      Type wrapperType = getWrapperType();
      Type wrapperArrayType = Wrappers.valueArrayType(wrapperType);
      
      JExpr newArray = Expressions.newArray(wrapperType, 1);
      
      JLValue unitArray = allocator.reserve(decl.getNameIfPresent(), wrapperArrayType, newArray);
      
      return new DereferencedFatPtr(unitArray, Expressions.constantInt(0), valueFunction);

    } else {
      JLValue array = allocator.reserve(decl.getNameIfPresent(), arrayType);
      JLValue offset = allocator.reserveOffsetInt(decl.getNameIfPresent());

      return new FatPtrPair(valueFunction, array, offset);
    }
  }

  @Override
  public FatPtr providedGlobalVariable(GimpleVarDecl decl, Field javaField) {
    throw new UnsupportedOperationException("TODO");
  }

  public PrimitiveValue toInt(MethodGenerator mv, FatPtr fatPtrExpr) {
    // Converting pointers to integers and vice-versa is implementation-defined
    // So we will define an implementation that supports at least one useful case spotted in S4Vectors:
    // double a[] = {1,2,3,4};
    // double *start = a;
    // double *end = p+4;
    // int length = (start-end)
    FatPtrPair pair = fatPtrExpr.toPair(mv);
    JExpr offset = pair.getOffset();
    JExpr offsetInBytes = Expressions.product(offset, valueFunction.getArrayElementBytes());

    return new PrimitiveValue(new GimpleIntegerType(32), offsetInBytes);
  }

  @Override
  public FatPtr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new FatPtrFieldStrategy(className, valueFunction, fieldName, arrayType);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, new FatPtrValueFunction(valueFunction));
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
  public FatPtr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    // Some C code tries to be tricky and only allocate *part* of a structure.
    // We will try to handle this by always rounding from zero up to one.
    JExpr length = Expressions.divide(sizeInBytes, valueFunction.getArrayElementBytes());
    JExpr ceil = Expressions.max(length, constantInt(1));
    
    return FatPtrMalloc.alloc(mv, valueFunction, ceil);
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(new FatPtrValueFunction(valueFunction), indirectionLevel + 1);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, new FatPtrValueFunction(valueFunction));
  }

  @Override
  public FatPtr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    return value.toFatPtrExpr(this.valueFunction);
  }

  @Override
  public FatPtr pointerPlus(MethodGenerator mv, FatPtr pointer, JExpr offsetInBytes) {
    FatPtrPair pointerPair = pointer.toPair(mv);
    JExpr offsetInArrayElements = Expressions.divide(offsetInBytes, valueFunction.getArrayElementBytes());
    JExpr newOffset = Expressions.sum(pointerPair.getOffset(), offsetInArrayElements);
    return new FatPtrPair(valueFunction, pointerPair.getArray(), newOffset);
  }



  @Override
  public FatPtr nullPointer() {
    return FatPtrPair.nullPtr(valueFunction);
  }

  private Type getWrapperType() {
    return Wrappers.wrapperType(valueFunction.getValueType());
  }

  @Override
  public String toString() {
    return "FatPtrStrategy[" + valueFunction + "]";
  }
}
