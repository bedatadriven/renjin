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

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrMalloc;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;

/**
 * Strategy for array fields
 */
public class ArrayField extends SingleFieldStrategy {

  private GimpleArrayType arrayType;
  private int arrayLength;
  private final ValueFunction valueFunction;
  
  public ArrayField(Type declaringClass, String name, int arrayLength, GimpleArrayType arrayType, ValueFunction valueFunction) {
    super(declaringClass, name, Wrappers.valueArrayType(valueFunction.getValueType()));
    this.arrayLength = arrayLength;
    this.arrayType = arrayType;
    this.valueFunction = valueFunction;
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JExpr newArray = FatPtrMalloc.allocArray(mv, valueFunction, 
        constantInt(arrayLength * valueFunction.getElementLength()));
    JLValue arrayField = Expressions.field(Expressions.thisValue(this.ownerClass), fieldType, fieldName);
    
    arrayField.store(mv, newArray);
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, GimpleType expectedType) {
    JExpr arrayExpr = Expressions.field(instance, fieldType, fieldName);
    JExpr offsetExpr = constantInt(offset / 8 / valueFunction.getArrayElementBytes());
    
    if(expectedType instanceof PrimitiveTypeStrategy) {
      PrimitiveTypeStrategy primitiveTypeStrategy = (PrimitiveTypeStrategy) expectedType;
      if(!primitiveTypeStrategy.getJvmType().equals(valueFunction.getValueType())) {
        throw new InternalCompilerException("TODO: " + valueFunction.getValueType() +
            "[] => " + primitiveTypeStrategy.getType());
      }
      
      return primitiveTypeStrategy.getValueFunction().dereference(arrayExpr, offsetExpr);
    
    } else if(expectedType instanceof ArrayTypeStrategy) {
      return new FatArrayExpr(arrayType, valueFunction, arrayLength, arrayExpr, offsetExpr);
    
    } else {
      throw new UnsupportedOperationException("expectedType: " + expectedType);
    }
    
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    JExpr sourceArray = Expressions.field(source, fieldType, fieldName);
    JExpr destArray = Expressions.field(dest, fieldType, fieldName);
    
    valueFunction.memoryCopy(mv, 
        destArray, constantInt(0),  
        sourceArray, constantInt(0), 
        constantInt(arrayLength));
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    JLValue arrayFieldExpr = Expressions.field(instance, fieldType, fieldName);
    valueFunction.memorySet(mv, arrayFieldExpr, Expressions.zero(), byteValue, byteCount);
  }
}
