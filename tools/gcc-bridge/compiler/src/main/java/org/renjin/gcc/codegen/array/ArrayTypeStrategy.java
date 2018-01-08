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
package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;


/**
 * Strategy for arrays with a fixed length, known at compile time.
 */
public class ArrayTypeStrategy implements TypeStrategy<FatArrayExpr> {

  private static final int MAX_UNROLL = 5;
  private final int arrayLength;
  private boolean parameterWrapped = true;
  private GimpleArrayType arrayType;
  private ValueFunction elementValueFunction;
  private ValueFunction arrayValueFunction;

  public ArrayTypeStrategy(GimpleArrayType arrayType, ValueFunction elementValueFunction) {
    this(arrayType, arrayType.getElementCount(), elementValueFunction);
  }
  
  public ArrayTypeStrategy(GimpleArrayType arrayType, int totalArrayLength, ValueFunction elementValueFunction) {
    this.arrayType = arrayType;
    this.elementValueFunction = elementValueFunction;
    this.arrayLength = totalArrayLength;
    this.arrayValueFunction = new ArrayValueFunction(arrayType, elementValueFunction);
  }

  public int getArrayLength() {
    return arrayLength;
  }

  public Type getElementType() {
    return elementValueFunction.getValueType();
  }
  
  public boolean isParameterWrapped() {
    return parameterWrapped;
  }

  public ArrayTypeStrategy setParameterWrapped(boolean parameterWrapped) {
    this.parameterWrapped = parameterWrapped;
    return this;
  }

  public ValueFunction getValueFunction() {
    return arrayValueFunction;
  }
  
  @Override
  public VPtrStrategy pointerTo() {
    return new VPtrStrategy(arrayType);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    // Multidimensional arrays are layed out in contiguous memory blocks
    if(arrayType.isStatic()) {
      return new ArrayTypeStrategy(arrayType, arrayLength * arrayType.getElementCount(), arrayValueFunction);
      
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }

  @Override
  public FatArrayExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    return value.toArrayExpr();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return fieldGenerator(className, fieldName);
  }

  @Override
  public FatArrayExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    Type arrayType = Wrappers.valueArrayType(elementValueFunction.getValueType());

    JExpr array = allocator.reserve(decl.getNameIfPresent(), arrayType, allocArray(arrayLength));
    JExpr offset = Expressions.zero();

    return new FatArrayExpr(this.arrayType, elementValueFunction, arrayLength, array, offset);
  }

  @Override
  public FatArrayExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    return new FatArrayExpr(this.arrayType, elementValueFunction, arrayLength, expr, Expressions.zero());
  }

  private JExpr allocArray(int arrayLength) {
    Preconditions.checkArgument(arrayLength >= 0);

    if(elementValueFunction.getValueConstructor().isPresent()) {
      // For reference types like records or fat pointers we have to 
      // initialize each element of the array
      if(arrayLength < MAX_UNROLL) {

        List<JExpr> valueConstructors = Lists.newArrayList();
        for (int i = 0; i < arrayLength; i++) {
          valueConstructors.add(elementValueFunction.getValueConstructor().get());
        }
        return Expressions.newArray(elementValueFunction.getValueType(), valueConstructors);

      } else {
        return new ArrayInitLoop(elementValueFunction, arrayLength);
      }

    } else {
      // For primitive types, we can just allocate the array
      return Expressions.newArray(elementValueFunction.getValueType(), arrayLength);
    }
  }
  
  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    throw new UnsupportedOperationException("TODO");
  }


  @Override
  public FatArrayExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor constructor) {
    List<JExpr> values = Lists.newArrayList();
    addElementConstructors(values, exprFactory, constructor);

    JExpr array = Expressions.newArray(elementValueFunction.getValueType(), arrayLength, values);
    JExpr offset = Expressions.zero();

    return new FatArrayExpr(arrayType, elementValueFunction, arrayLength, array, offset);
  }

  private void addElementConstructors(List<JExpr> values, ExprFactory exprFactory, GimpleConstructor constructor) {
    for (GimpleConstructor.Element element : constructor.getElements()) {
      if(element.getValue() instanceof GimpleConstructor &&
          element.getValue().getType() instanceof GimpleArrayType) {
        GimpleConstructor elementConstructor = (GimpleConstructor) element.getValue();

        addElementConstructors(values, exprFactory, elementConstructor);

      } else {
        GExpr elementExpr = exprFactory.findGenerator(element.getValue());
        List<JExpr> arrayValues = elementValueFunction.toArrayValues(elementExpr);
        values.addAll(arrayValues);
      }
    }
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new ArrayField(className, fieldName, arrayLength, arrayType, elementValueFunction);
  }

  public GimpleArrayType getGimpleType() {
    return arrayType;
  }
}
