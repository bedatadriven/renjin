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
import org.renjin.gcc.codegen.array.FatArrayExpr;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtrExpr;
import org.renjin.gcc.codegen.expr.ConstantValue;
import org.renjin.gcc.codegen.type.primitive.PrimitiveExpr;
import org.renjin.gcc.codegen.type.primitive.UnsignedInt32Expr;
import org.renjin.gcc.codegen.type.record.ProvidedPtrExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtrExpr;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.vptr.VArrayExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A FatPtr expression compiled as a pair of array and an offset.
 */
public final class FatPtrPair implements FatPtr, PtrExpr {

  private ValueFunction valueFunction;
  private JExpr array;
  private JExpr offset;
  private GExpr address;

  public FatPtrPair(ValueFunction valueFunction, @Nullable GExpr address, @Nonnull JExpr array, @Nonnull JExpr offset) {
    this.valueFunction = valueFunction;
    Preconditions.checkNotNull(array, "array");
    Preconditions.checkNotNull(offset, "offset");

    this.address = address;
    this.array = array;
    this.offset = offset;
  }

  public FatPtrPair(ValueFunction valueFunction, @Nonnull JExpr array, @Nonnull JExpr offset) {
    this(valueFunction, null, array, offset);
  }
  
  public FatPtrPair(ValueFunction valueFunction, JExpr array) {
    this(valueFunction, array, Expressions.zero());
  }

  @Nonnull
  public JExpr getArray() {
    return array;
  }

  @Nonnull
  public JExpr getOffset() {
    return offset;
  }

  @Override
  public Type getValueType() {
    String arrayDescriptor = array.getType().getDescriptor();
    Preconditions.checkState(arrayDescriptor.startsWith("["));
    return Type.getType(arrayDescriptor.substring(1));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void store(MethodGenerator mv, GExpr rhsExpr) {

    if(rhsExpr instanceof VoidPtrExpr) {
      VoidPtrExpr ptr = (VoidPtrExpr) rhsExpr;
      Type wrapperType = Wrappers.wrapperType(getValueType());

      // Casting a void* to a FatPtr Wrapper like DoublePtr requires
      // runtime support because we may need to trigger a MallocThunk.
      ptr.jexpr().load(mv);
      mv.invokestatic(wrapperType, "cast", Type.getMethodDescriptor(wrapperType, Type.getType(Object.class)));

      LocalVarAllocator.LocalVar tempVar = mv.getLocalVarAllocator().reserve(wrapperType);
      tempVar.store(mv);

      JExpr arrayField = Wrappers.arrayField(tempVar, getValueType());
      JExpr offsetField = Wrappers.offsetField(tempVar);

      store(mv, arrayField, offsetField);

    } else if(rhsExpr instanceof WrappedFatPtrExpr) {
      // Need to do a null check here first
      Label nullLabel = new Label();
      Label exitLabel = new Label();
      
      // Check to see if the pointer to the wrapper is null
      ((WrappedFatPtrExpr) rhsExpr).wrap().load(mv);
      mv.ifnull(nullLabel);
      
      // If non-null, break out into array and offset fields
      FatPtrPair pair = ((WrappedFatPtrExpr) rhsExpr).toPair(mv);
      store(mv, pair.getArray(), pair.getOffset());
      mv.goTo(exitLabel);
      
      // If null, store null and zero offset
      mv.mark(nullLabel);
      store(mv, Expressions.nullRef(array.getType()), Expressions.constantInt(0));
      
      // Done.
      mv.mark(exitLabel);
      
      
    } else if(rhsExpr instanceof FatPtr) {
      FatPtrPair pair = ((FatPtr) rhsExpr).toPair(mv);
      store(mv, pair.getArray(), pair.getOffset());

    } else {
      throw new UnsupportedOperationException("rhs: " + rhsExpr);
    }
  }

  @Override
  public GExpr valueOf(GimpleType expectedType) {
    return valueFunction.dereference(array, offset);
  }

  @Override
  public ConditionGenerator comparePointer(MethodGenerator mv, GimpleOp op, GExpr otherPointer) {
    if(otherPointer instanceof VPtrExpr) {
      return toVPtrExpr().comparePointer(mv, op, otherPointer);
    }

    return new FatPtrConditionGenerator(op, this, otherPointer.toFatPtrExpr(valueFunction).toPair(mv));
  }

  private void store(MethodGenerator mv, JExpr arrayRhs, JExpr offsetRhs) {
    if (!(array instanceof JLValue)) {
      throw new InternalCompilerException(array + " is not an LValue");
    }
    ((JLValue) array).store(mv, Expressions.cast(arrayRhs, array.getType()));

    // Normally, the offset must also be an LValue, but the exception 
    // is that if both the lhs and rhs are constants, and they are equal
    if (offset instanceof ConstantValue &&
        offset.equals(offsetRhs)) {
      // No assignment neccessary
      return;
    }

    // Otherwise the offset must also be assignable
    if (!(offset instanceof JLValue)) {
      throw new InternalCompilerException(offset + " offset is not an Lvalue");
    }
    ((JLValue) offset).store(mv, offsetRhs);
  }

  @Override
  public VPtrExpr toVPtrExpr() {
    return valueFunction.toVPtr(array, offset);
  }

  @Override
  public ProvidedPtrExpr toProvidedPtrExpr(Type jvmType) {
    throw new UnsupportedOperationException("Unsupported cast");
  }

  @Override
  public FatPtr toFatPtrExpr(ValueFunction valueFunction) {
    return this;
  }

  @Override
  public VPtrRecordExpr toVPtrRecord(GimpleRecordType recordType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VArrayExpr toVArray(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public NumericExpr toNumericExpr() {
    throw new UnsupportedOperationException("TODO");
  }

  public JExpr wrap() {
    final Type wrapperType = Wrappers.wrapperType(getValueType());
    
    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return wrapperType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.anew(wrapperType);
        mv.dup();
        
        if(wrapperType.equals(Type.getType(ObjectPtr.class))) {
          if(valueFunction.getValueType().getSort() == Type.OBJECT) {
            mv.aconst(valueFunction.getValueType());
          } else {
            mv.aconst(null);
          }
          array.load(mv);
          offset.load(mv);
          mv.invokeconstructor(wrapperType, 
              Type.getType(Class.class), 
              Wrappers.fieldArrayType(wrapperType), 
              offset.getType());

        } else {

          array.load(mv);
          offset.load(mv);
          mv.invokeconstructor(wrapperType, Wrappers.fieldArrayType(wrapperType), offset.getType());
        }
      }
    };
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return this;
  }

  @Override
  public boolean isAddressable() {
    return address != null;
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }

  @Override
  public FunPtrExpr toFunPtr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FatArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PrimitiveExpr toPrimitiveExpr() throws UnsupportedCastException {
    // Converting pointers to integers and vice-versa is implementation-defined
    // So we will define an implementation that supports at least one useful case spotted in S4Vectors:
    // double a[] = {1,2,3,4};
    // double *start = a;
    // double *end = p+4;
    // int length = (start-end)
    JExpr offsetInBytes = Expressions.product(offset, valueFunction.getArrayElementBytes());

    PrimitiveExpr primitiveExpr = new UnsignedInt32Expr(offsetInBytes);

    return primitiveExpr.toPrimitiveExpr();
  }

  @Override
  public VoidPtrExpr toVoidPtrExpr() throws UnsupportedCastException {
    return new VoidPtrExpr(wrap());
  }

  public JExpr at(int i) {
    return Expressions.elementAt(array, Expressions.sum(offset, i));
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    array.load(mv);
    mv.ifnull(label);
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, PtrExpr otherPointer, JExpr n) {
    // We can convert from FatPtrPair -> VPtr but not the other way around
    if(otherPointer instanceof VPtrExpr) {
      return toVPtrExpr().memoryCompare(mv, otherPointer, n);
    }

    return new FatPtrMemCmp(this, otherPointer.toFatPtrExpr(valueFunction).toPair(mv), n);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr byteValue, JExpr length) {
    // Delegate to the value function.
    valueFunction.memorySet(mv, array, offset, byteValue, length);
  }

  @Override
  public PtrExpr realloc(MethodGenerator mv, JExpr newSizeInBytes) {
    JExpr sizeInElements = Expressions.divide(newSizeInBytes, valueFunction.getArrayElementBytes());
    JExpr array = new FatPtrRealloc(this, sizeInElements);
    JExpr offset = Expressions.zero();

    return new FatPtrPair(valueFunction, array, offset);
  }

  @Override
  public PtrExpr pointerPlus(MethodGenerator mv, JExpr offsetInBytes) {
    JExpr offsetInArrayElements = Expressions.divide(offsetInBytes, valueFunction.getArrayElementBytes());
    JExpr newOffset = Expressions.sum(this.offset, offsetInArrayElements);
    return new FatPtrPair(valueFunction, this.array, newOffset);
  }

  public static FatPtr nullPtr(ValueFunction valueFunction) {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    JExpr nullArray = Expressions.nullRef(arrayType);
    return new FatPtrPair(valueFunction, nullArray);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, PtrExpr source, JExpr lengthBytes, boolean buffer) {


    // If we are copy from a VPtr source, then the easiest is probably to do...
    // double a[];
    // int a$offset;
    // new DoublePtr(a, a$offset).memcpy(source, lengthBytes);

    if(source instanceof VPtrExpr) {
      this.toVPtrExpr().memoryCopy(mv, source, lengthBytes, buffer);

    } else {
      FatPtrPair sourcePair = source.toFatPtrExpr(valueFunction).toPair(mv);

      // Convert bytes -> value counts
      JExpr valueCount = computeElementsToCopy(lengthBytes);

      valueFunction.memoryCopy(mv,
          array, offset,
          sourcePair.getArray(), sourcePair.getOffset(), valueCount);
    }
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

}
