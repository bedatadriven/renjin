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
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.*;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordConstructor;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import javax.annotation.Nonnull;


public class RecordUnitPtrStrategy implements PointerTypeStrategy<RecordUnitPtr>, SimpleTypeStrategy<RecordUnitPtr> {
  
  private RecordClassTypeStrategy strategy;
  private RecordUnitPtrValueFunction valueFunction;
  
  public RecordUnitPtrStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
    this.valueFunction = new RecordUnitPtrValueFunction(strategy.getJvmType());
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RecordUnitPtrParam(this);
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new RecordUnitPtrField(className, fieldName, strategy.getJvmType());
  }

  @Override
  public RecordUnitPtr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
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
  public RecordUnitPtr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(value instanceof FatPtr) {
      FatPtrPair ptr = ((FatPtr) value).toPair(mv);
      
      // TODO
      // Currently we punt until runtime by triggering a ClassCastException
      return new RecordUnitPtr(Expressions.nullRef(strategy.getJvmType()));
      
    } else if(typeStrategy instanceof RecordUnitPtrStrategy) {
      RecordUnitPtr ptrExpr = (RecordUnitPtr) value;
      return new RecordUnitPtr(Expressions.cast(ptrExpr.unwrap(), strategy.getJvmType()));
      
    } else if(value instanceof VoidPtr) {
      LocalVarAllocator.LocalVar var = mv.getLocalVarAllocator().reserve(strategy.getJvmType());

      ((VoidPtr) value).unwrap().load(mv);
      mv.visitLdcInsn(strategy.getJvmType());
      mv.invokestatic(ObjectPtr.class, "castUnit",
          Type.getMethodDescriptor(Type.getType(Object.class),
              Type.getType(Object.class), Type.getType(Class.class)));
      mv.checkcast(strategy.getJvmType());
      mv.store(var.getIndex(), strategy.getJvmType());
      return new RecordUnitPtr(var);
    }
    throw new UnsupportedCastException();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new RecordUnitPtrReturnStrategy(strategy.getJvmType());
  }

  @Override
  public ValueFunction getValueFunction() {
    return valueFunction;
  }

  @Override
  public RecordUnitPtr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {

      // Declare this as a Unit array so that we can get a FatPtrExpr if needed
      JExpr unitArray = allocator.reserveUnitArray(decl.getName(), strategy.getJvmType(), Optional.<JExpr>absent());

      FatPtrPair address = new FatPtrPair(valueFunction, unitArray);
      ArrayElement instance = Expressions.elementAt(unitArray, 0);
      
      return new RecordUnitPtr(instance, address);
      
    } else {
      return new RecordUnitPtr(allocator.reserve(decl.getNameIfPresent(), strategy.getJvmType()));
    }
  }

  @Override
  public RecordUnitPtr malloc(MethodGenerator mv, JExpr sizeInBytes) {

    if (isUnitConstant(sizeInBytes)) {
      throw new InternalCompilerException(getClass().getSimpleName() + " does not support (T)malloc(size) where " +
          "size != sizeof(T). This is probably because of a mistake in the choice of strategy by the compiler.");
    }
    return new RecordUnitPtr(new RecordConstructor(strategy));
  }

  @Override
  public RecordUnitPtr realloc(MethodGenerator mv, RecordUnitPtr pointer, JExpr newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordUnitPtr pointerPlus(MethodGenerator mv, final RecordUnitPtr pointer, final JExpr offsetInBytes) {
    // According to our analysis conducted before-hand, there should be no pointer
    // to a sequence of records of this type with more than one record, so the result should
    // be undefined.
    JExpr expr = new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return pointer.unwrap().getType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        Label zero = new Label();
        offsetInBytes.load(mv);
        mv.ifeq(zero);
        mv.anew(Type.getType(ArrayIndexOutOfBoundsException.class));
        mv.dup();
        mv.invokeconstructor(Type.getType(ArrayIndexOutOfBoundsException.class));
        mv.athrow();
        mv.mark(zero);
        pointer.unwrap().load(mv);
      }
    };
    
    return new RecordUnitPtr(expr);
  }

  @Override
  public RecordUnitPtr nullPointer() {
    return new RecordUnitPtr(Expressions.nullRef(strategy.getJvmType()));
  }

  @Override
  public ConditionGenerator comparePointers(MethodGenerator mv, GimpleOp op, RecordUnitPtr x, RecordUnitPtr y) {
    return new RefConditionGenerator(op, x.unwrap(), y.unwrap());
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, RecordUnitPtr p1, RecordUnitPtr p2, JExpr n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, RecordUnitPtr destination, RecordUnitPtr source, JExpr length, boolean buffer) {

    Type recordType = strategy.getJvmType();

    destination.unwrap().load(mv);
    source.unwrap().load(mv);
    mv.invokevirtual(recordType, "set", Type.getMethodDescriptor(Type.VOID_TYPE, recordType), false);
  }

  @Override
  public void memorySet(MethodGenerator mv, RecordUnitPtr pointer, JExpr byteValue, JExpr length) {
    pointer.unwrap().load(mv);
    byteValue.load(mv);
    length.load(mv);
    mv.invokevirtual(strategy.getJvmType(), "memset", 
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE), false);
  }

  @Override
  public VoidPtr toVoidPointer(RecordUnitPtr ptrExpr) {
    return new VoidPtr(ptrExpr.unwrap());
  }

  @Override
  public RecordUnitPtr unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer) {
    return new RecordUnitPtr(Expressions.cast(voidPointer, getJvmType()));
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
  public RecordUnitPtr wrap(JExpr expr) {
    return new RecordUnitPtr(expr);
  }

  @Override
  public String toString() {
    return "RecordUnitPtrStrategy[" + strategy.getRecordTypeDef().getName() + "]";
  }
}
