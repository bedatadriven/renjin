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
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


/**
 * Strategy for handling pointers of unknown type.
 * 
 * <p>GCC Bridge compiles {@code void *} types as values of type {@code java.lang.Object}.
 * Void pointers may point a Fat Pointer object such as {@link org.renjin.gcc.runtime.DoublePtr}, 
 * to a {@link java.lang.invoke.MethodHandle}, or to record type for records that use the 
 * {@link org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy}.</p>
 */
public class VoidPtrStrategy implements PointerTypeStrategy<VoidPtr>, SimpleTypeStrategy<VoidPtr> {
  
  public static final Type OBJECT_TYPE = Type.getType(Object.class);
  
  @Override
  public VoidPtr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    return new org.renjin.gcc.codegen.type.voidt.VoidPtr(new NewMallocThunkExpr(sizeInBytes));
  }

  @Override
  public VoidPtr newArray(MethodGenerator mv, JExpr count) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VoidPtr realloc(MethodGenerator mv, final VoidPtr pointer, JExpr newSizeInBytes) {
    return new VoidPtr(new VoidPtrRealloc(pointer.unwrap(), newSizeInBytes));
  }

  @Override
  public VoidPtr pointerPlus(MethodGenerator mv, final VoidPtr pointer, final JExpr offsetInBytes) {
    // We have to rely on run-time support for this because we don't know
    // what kind of pointer is stored here
    return new VoidPtr(new JExpr() {

      @Nonnull
      @Override
      public Type getType() {
        return Type.getType(Object.class);
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        pointer.unwrap().load(mv);
        offsetInBytes.load(mv);
        mv.invokestatic(org.renjin.gcc.runtime.VoidPtr.class, "pointerPlus",
            Type.getMethodDescriptor(Type.getType(Object.class), 
                Type.getType(Object.class), Type.INT_TYPE));
      }
    });
  }

  @Override
  public VoidPtr nullPointer() {
    return new VoidPtr(Expressions.nullRef(Type.getType(Object.class)));
  }

  @Override
  public ConditionGenerator comparePointers(MethodGenerator mv, GimpleOp op, VoidPtr x, VoidPtr y) {
    return new VoidPtrComparison(op, x.unwrap(), y.unwrap());
  }

  @Override
  public JExpr memoryCompare(MethodGenerator mv, VoidPtr p1, VoidPtr p2, JExpr n) {
    return new VoidPtrMemCmp(p1.unwrap(), p2.unwrap(), n);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, VoidPtr destination, VoidPtr source, JExpr length, boolean buffer) {
    
    destination.unwrap().load(mv);
    source.unwrap().load(mv);
    length.load(mv);
    
    mv.invokestatic(org.renjin.gcc.runtime.VoidPtr.class, "memcpy", 
        Type.getMethodDescriptor(Type.VOID_TYPE, 
            Type.getType(Object.class), Type.getType(Object.class), Type.INT_TYPE));
  }

  @Override
  public void memorySet(MethodGenerator mv, VoidPtr pointer, JExpr byteValue, JExpr length) {
    pointer.unwrap().load(mv);
    byteValue.load(mv);
    length.load(mv);
    
    mv.invokestatic(org.renjin.gcc.runtime.VoidPtr.class, "memset",
        Type.getMethodDescriptor(Type.VOID_TYPE,
            Type.getType(Object.class), 
            Type.INT_TYPE, 
            Type.INT_TYPE));
  }

  @Override
  public VoidPtr toVoidPointer(VoidPtr ptrExpr) {
    return ptrExpr;
  }

  @Override
  public VoidPtr unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer) {
    return new VoidPtr(voidPointer);
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RefPtrParamStrategy<>(this);
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
  public VoidPtr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      Type objectArrayType = Type.getType("[Ljava/lang/Object;");
      JLValue unitArray = allocator.reserve(decl.getName(), objectArrayType, Expressions.newArray(Object.class, 1));
      FatPtrPair address = new FatPtrPair(new VoidPtrValueFunction(), unitArray);
      JExpr value = Expressions.elementAt(unitArray, 0);
      
      return new VoidPtr(value, address);
    
    } else {
      
      return new VoidPtr(allocator.reserve(decl.getNameIfPresent(), Type.getType(Object.class)));
    }
  }

  @Override
  public VoidPtr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
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
  public VoidPtr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    
    if(value instanceof VoidPtr) {
      return (VoidPtr) value;
    }
    
    if(typeStrategy instanceof PointerTypeStrategy) {
      return ((PointerTypeStrategy) typeStrategy).toVoidPointer(value);
    }
    throw new UnsupportedCastException();
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
  public VoidPtr wrap(JExpr expr) {
    return new VoidPtr(expr);
  }
}
