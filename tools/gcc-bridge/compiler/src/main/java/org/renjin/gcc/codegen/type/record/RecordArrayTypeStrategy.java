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
import org.renjin.gcc.codegen.array.ArrayExpr;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValueFunction;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

import static org.renjin.gcc.codegen.expr.Expressions.*;

/**
 * Represents a record with a primitive array.
 * 
 * <p>This strategy only works for records that have only primitives fields of the same type. For example,
 * the C struct:</p>
 * <pre>
 *   struct point {
 *     double x;
 *     double y;
 *   }
 * </pre>
 * <p>Can be compiled using a simple {@code double[]} instead of a full-blown JVM class. This makes it 
 * easy to allow pointers to records of such types to be cast back and forth between {@code double*} pointers
 * (or {@code int*} etc.)</p>
 */
public class RecordArrayTypeStrategy extends RecordTypeStrategy<RecordArrayExpr> {
  
  private Type fieldType;
  private Type arrayType;
  private int arrayLength;
  private final RecordArrayValueFunction valueFunction;
  
  public RecordArrayTypeStrategy(GimpleRecordTypeDef recordTypeDef, Type fieldType) {
    super(recordTypeDef);
    this.fieldType = fieldType;
    arrayType = Wrappers.valueArrayType(fieldType);
    arrayLength = computeArrayLength(recordTypeDef, fieldType);
    valueFunction = new RecordArrayValueFunction(fieldType, arrayLength, new GimpleRecordType(recordTypeDef));
  }

  private static int computeArrayLength(GimpleRecordTypeDef recordTypeDef, Type fieldType) {
    int recordSize = recordTypeDef.getSize();
    int elementSize = GimplePrimitiveType.fromJvmType(fieldType).getSize();
    if(elementSize == 0) {
      throw new IllegalStateException("sizeof(" + fieldType + ") = 0");
    }
    return recordSize / elementSize;
  }

  @Override
  public GExpr memberOf(MethodGenerator mv, RecordArrayExpr instance, int fieldOffsetBits, int size, TypeStrategy fieldTypeStrategy) {
    
    // All the fields in this record are necessarily primitives, so we need
    // simple to retrieve the element from within the array that corresponds to
    // the given field name
    JExpr array = instance.getArray();
    JExpr fieldOffset = constantInt(fieldOffsetBits / 8 / elementSizeInBytes());
    JExpr offset = sum(instance.getOffset(), fieldOffset);

    // Because this value is backed by an array, we can also make it addressable. 
    FatPtrPair address = new FatPtrPair(valueFunction, array, offset);
    
    // The members of this record may be either primitives, or arrays of primitives,
    // and it actually doesn't matter to us.
    
    if(fieldTypeStrategy instanceof PrimitiveTypeStrategy) {
      Type expectedType = ((PrimitiveTypeStrategy) fieldTypeStrategy).getJvmType();

      // Return a single primitive value
      if(expectedType.equals(fieldType)) {
        JExpr value = elementAt(array, offset);
        return new PrimitiveValue(value, address);
      } else if (fieldType.equals(Type.BYTE_TYPE) && expectedType.equals(Type.INT_TYPE)) {
        return new PrimitiveValue(new ByteArrayAsInt(array, offset));
        
      } else if (fieldType.equals(Type.LONG_TYPE) && expectedType.equals(Type.DOUBLE_TYPE)) {
        JLValue value = elementAt(array, offset);
        return new PrimitiveValue(new LongAsDouble(value));
        
      } else {
        throw new UnsupportedOperationException("TODO: " + fieldType + " -> " + expectedType);
      }

    } else if(fieldTypeStrategy instanceof ArrayTypeStrategy) {
      ArrayTypeStrategy arrayType = (ArrayTypeStrategy) fieldTypeStrategy;
      Type expectedType = arrayType.getElementType();

      return new ArrayExpr(new PrimitiveValueFunction(expectedType), arrayType.getArrayLength(), array, offset);


    } else if(fieldTypeStrategy instanceof RecordArrayTypeStrategy) {
      RecordArrayTypeStrategy recordArrayTypeStrategy = (RecordArrayTypeStrategy) fieldTypeStrategy;
      if(!recordArrayTypeStrategy.fieldType.equals(this.fieldType)) {
        throw new InternalCompilerException("Cannot access member of type " + fieldTypeStrategy +   
            " from record array of type " + this);
      }
      
      return new RecordArrayExpr(recordArrayTypeStrategy.valueFunction, array, fieldOffset, 
          recordArrayTypeStrategy.arrayLength);
      
    } else {
      // Return an array that starts at this point 
      return new FatPtrPair(valueFunction, address, array, offset);
    }
    
  }

  private int elementSizeInBytes() {
    return GimplePrimitiveType.fromJvmType(fieldType).sizeOf();
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RecordArrayParamStrategy(valueFunction, arrayType, arrayLength);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new RecordArrayReturnStrategy(valueFunction, arrayType, arrayLength);
  }

  @Override
  public ValueFunction getValueFunction() {
    return valueFunction;
  }

  @Override
  public RecordArrayExpr variable(GimpleVarDecl decl, VarAllocator allocator) {

    JExpr newArray = newArray(fieldType, arrayLength);
    JLValue arrayVar = allocator.reserve(decl.getName(), arrayType, newArray);
    
    return new RecordArrayExpr(valueFunction, arrayVar, arrayLength);
  }

  @Override
  public RecordArrayExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    return new RecordArrayExpr(valueFunction, newArray(fieldType, arrayLength), arrayLength);
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, final String fieldName) {
    return new RecordArrayField(className, fieldName, arrayType.getElementType(), arrayLength, this.recordType);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return fieldGenerator(className, fieldName);
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new FatPtrStrategy(valueFunction, 1);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, valueFunction);
  }

  @Override
  public RecordArrayExpr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof RecordArrayTypeStrategy) {
      return (RecordArrayExpr) value;
    }  else if(typeStrategy instanceof FatPtrStrategy) {
      FatPtrPair fatPtrExpr = (FatPtrPair) value;
      return new RecordArrayExpr(valueFunction, fatPtrExpr.getArray(), fatPtrExpr.getOffset(), arrayLength);
    }
    throw new UnsupportedCastException();
  }

  @Override
  public String toString() {
    return "RecordArrayTypeStrategy[" + valueFunction.getValueType() + "]";
  }
}
