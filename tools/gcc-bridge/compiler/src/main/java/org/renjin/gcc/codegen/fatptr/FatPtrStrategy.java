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

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordValue;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;
import static org.renjin.gcc.codegen.expr.Expressions.newArray;
import static org.renjin.repackaged.asm.Type.OBJECT;

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

      Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
      Type wrapperArrayType = Wrappers.valueArrayType(wrapperType);
      
      JExpr newArray = newArray(wrapperType, 1);
      
      JLValue unitArray = allocator.reserve(decl.getName(), wrapperArrayType, newArray);
      
      return new DereferencedFatPtr(unitArray, Expressions.constantInt(0), valueFunction);

    } else {
      JLValue array = allocator.reserve(decl.getNameIfPresent(), arrayType);
      JLValue offset = allocator.reserveOffsetInt(decl.getNameIfPresent());

      return new FatPtrPair(valueFunction, array, offset);
    }
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

    return new PrimitiveValue(offsetInBytes);
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
    if(isParametersWrapped()) {
      if(valueFunction.getValueType().getSort() == OBJECT) {
        
      }
      return new WrappedFatPtrParamStrategy(valueFunction);
    } else {
      return new FatPtrParamStrategy(valueFunction);
    }
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new FatPtrReturnStrategy(valueFunction);
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
  public FatPtr realloc(MethodGenerator mv, FatPtr pointer, JExpr newSizeInBytes) {
    JExpr sizeInElements = Expressions.divide(newSizeInBytes, valueFunction.getArrayElementBytes());
    JExpr array = new FatPtrRealloc(pointer.toPair(mv), sizeInElements);
    JExpr offset = Expressions.zero();
    
    return new FatPtrPair(valueFunction, array, offset);
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
    if(value instanceof VoidPtr) {
      VoidPtr ptrExpr = (VoidPtr) value;
      JExpr wrapperInstance = Wrappers.cast(valueFunction.getValueType(), ptrExpr.unwrap());

      JExpr arrayField = Wrappers.arrayField(wrapperInstance);
      JExpr offsetField = Wrappers.offsetField(wrapperInstance);

      return new FatPtrPair(valueFunction, arrayField, offsetField);
      
    
    } else if(value instanceof FatPtr) {
      // allow any casts between FatPtrs. though runtime errors may occur
      // (The JVM simply won't allow us to cast an int* to a double*)
      FatPtrPair ptrExpr = ((FatPtr) value).toPair(mv);
      GExpr address = null;
      if (ptrExpr.isAddressable()) {
        address = ptrExpr.addressOf();
      }
      JExpr castedArray = Expressions.uncheckedCast(ptrExpr.getArray(), arrayType);
      JExpr offset = ptrExpr.getOffset();

      return new FatPtrPair(valueFunction, address, castedArray, offset);

    } else if(typeStrategy instanceof RecordUnitPtrStrategy) {
      RecordUnitPtr ptr = (RecordUnitPtr) value;
      if(valueFunction.getValueType().getSort() != Type.OBJECT) {
        throw new InternalCompilerException("Cannot cast value using RecordUnitPtrStrategy to array of " + 
            valueFunction.getValueType());
      }
      JExpr ref = Expressions.cast(ptr.unwrap(), valueFunction.getValueType());
      JExpr newArray = Expressions.newArray(ref);
      
      return new FatPtrPair(valueFunction, newArray);

    } else if(typeStrategy instanceof RecordClassTypeStrategy) {

      // We can make this cast work if the first field of the record type is a compatible pointer type
      RecordClassTypeStrategy recordClassTypeStrategy = (RecordClassTypeStrategy) typeStrategy;
      return (FatPtr)recordClassTypeStrategy.memberOf(mv, ((RecordValue) value), 0, 32, this);
    }
    
    throw new UnsupportedCastException();
  }

  @Override
  public FatPtr pointerPlus(MethodGenerator mv, FatPtr pointer, JExpr offsetInBytes) {
    FatPtrPair pointerPair = pointer.toPair(mv);
    JExpr offsetInArrayElements = Expressions.divide(offsetInBytes, valueFunction.getArrayElementBytes());
    JExpr newOffset = Expressions.sum(pointerPair.getOffset(), offsetInArrayElements);
    return new FatPtrPair(valueFunction, pointerPair.getArray(), newOffset);
  }

  @Override
  public ConditionGenerator comparePointers(MethodGenerator mv, GimpleOp op, FatPtr x, FatPtr y) {
    return new FatPtrConditionGenerator(op, x.toPair(mv), y.toPair(mv));
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, FatPtr p1, FatPtr p2, JExpr n) {
    return new FatPtrMemCmp(p1.toPair(mv), p2.toPair(mv), n);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, FatPtr destination, FatPtr source, JExpr lengthBytes, boolean buffer) {
    
    FatPtrPair destinationPair = destination.toPair(mv);
    FatPtrPair sourcePair = source.toPair(mv);
    
    // Convert bytes -> value counts
    JExpr valueCount = computeElementsToCopy(lengthBytes);
    
    valueFunction.memoryCopy(mv,
        destinationPair.getArray(), destinationPair.getOffset(),
        sourcePair.getArray(), sourcePair.getOffset(), valueCount);
  }

  private JExpr computeElementsToCopy(JExpr lengthBytes) {
    if(lengthBytes instanceof ConstantValue) {
      // It can be that the actual storage size of a record (struct)
      // is smaller than it's declared size, for example, a struct
      // with a 3 booleans will still have a size of 4 bytes for alignment purposes.
      
      // When COPYING a SINGLE element however, sometimes GCC it's infinite cleverness, 
      // will copy ONLY the actual number of bytes stored. 
      
      // Dividing this number by the declared size of the struct will result 
      // in ZERO and nothing will be copied. For this reason, we need a little
      // hack here for this very particular case.
      
      int numBytes = ((ConstantValue) lengthBytes).getIntValue();
      if(numBytes < valueFunction.getArrayElementBytes()) {
        return Expressions.constantInt(1);
      } 
    }
    
    return Expressions.divide(lengthBytes, valueFunction.getArrayElementBytes());
  }

  @Override
  public void memorySet(MethodGenerator mv, FatPtr pointer, JExpr byteValue, JExpr length) {

    // Delegate to the value function.
    FatPtrPair pointerPair = pointer.toPair(mv);
    valueFunction.memorySet(mv, 
        pointerPair.getArray(), 
        pointerPair.getOffset(), byteValue, length);
  }

  @Override
  public VoidPtr toVoidPointer(FatPtr ptrExpr) {
    return new VoidPtr(ptrExpr.wrap());
  }


  @Override
  public FatPtr nullPointer() {
    return FatPtrPair.nullPtr(valueFunction);
  }

  @Override
  public FatPtr unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer) {

    // cast the result to the wrapper type, e.g. ObjectPtr or DoublePtr
    Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
    JExpr wrapperPtr = Wrappers.cast(valueFunction.getValueType(), voidPointer);

    // Reserve a local variable to hold the result
    JLValue retVal = mv.getLocalVarAllocator().reserve(wrapperType);

    // store the result of the call to the temp variable
    retVal.store(mv, wrapperPtr);

    // Now unpack the array and offset into seperate local variables
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    JLValue arrayVar = mv.getLocalVarAllocator().reserve(arrayType);
    JLValue offsetVar = mv.getLocalVarAllocator().reserve(Type.INT_TYPE);
    
    arrayVar.store(mv, Wrappers.arrayField(retVal, valueFunction.getValueType()));
    offsetVar.store(mv, Wrappers.offsetField(retVal));
    
    return new FatPtrPair(valueFunction, arrayVar, offsetVar);
  }

  @Override
  public String toString() {
    return "FatPtrStrategy[" + valueFunction + "]";
  }
}
