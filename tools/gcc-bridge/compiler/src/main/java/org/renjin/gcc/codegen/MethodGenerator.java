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
package org.renjin.gcc.codegen;

import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Preconditions;

import java.util.Arrays;


public class MethodGenerator extends InstructionAdapter {

  private final LocalVarAllocator localVarAllocator = new LocalVarAllocator();

  public MethodGenerator(MethodVisitor mv) {
    super(Opcodes.ASM5, mv);
  }

  public LocalVarAllocator getLocalVarAllocator() {
    return localVarAllocator;
  }

  public void invokestatic(Class<?> ownerClass, String methodName, String descriptor) {
    invokestatic(Type.getInternalName(ownerClass), methodName, descriptor, false);
  }

  public void invokestatic(Type ownerClass, String methodName, String descriptor) {
    invokestatic(ownerClass.getInternalName(), methodName, descriptor, false);
  }

  public void invokeconstructor(Type ownerClass, Type... argumentTypes) {
    invokespecial(ownerClass.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, argumentTypes), false);
  }

  public void invokevirtual(Type declaringType, String methodName, String descriptor, boolean interfaceMethod) {
    invokevirtual(declaringType.getInternalName(),
        methodName,
        descriptor,
        interfaceMethod);
  }

  public void invokevirtual(Class<?> declaringClass, String methodName, Type returnType, Type... argumentTypes) {
    invokevirtual(Type.getType(declaringClass).getInternalName(),
        methodName,
        Type.getMethodDescriptor(returnType, argumentTypes),
        declaringClass.isInterface());
  }

  public void invokeinterface(Class<?> declaringClass, String methodName, Type returnType, Type... argumentTypes) {
    invokeinterface(Type.getType(declaringClass).getInternalName(),
        methodName,
        Type.getMethodDescriptor(returnType, argumentTypes));
  }
  
  public void invokeIdentityHashCode() {
    invokestatic(System.class, "identityHashCode", "(Ljava/lang/Object;)I");
  }
  
  public void putfield(Type declaringClass, String name, Type fieldType) {
    putfield(declaringClass.getInternalName(), name, fieldType.getDescriptor());
  }

  public void getfield(Type ownerClass, String fieldName, Type fieldType) {
    getfield(ownerClass.getInternalName(), fieldName, fieldType.getDescriptor());
  }

  public void pop(Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        // NOOP
        break;
    
      case Type.LONG:
      case Type.DOUBLE:
        pop2();
        break;
      
      default:
        pop();
    }
  }

  /**
   * Writes an invocation of {@link System#arraycopy(Object, int, Object, int, int)}.
   * 
   * @param      src      the source array.
   * @param      srcPos   starting position in the source array.
   * @param      dest     the destination array.
   * @param      destPos  starting position in the destination data.
   * @param      length   the number of array elements to be copied.
   */
  public void arrayCopy(JExpr src, JExpr srcPos, JExpr dest, JExpr destPos, JExpr length) {

    Preconditions.checkArgument(srcPos.getType().equals(Type.INT_TYPE), "srcPos must have type int");
    Preconditions.checkArgument(destPos.getType().equals(Type.INT_TYPE), "destPos must have type int");
    Preconditions.checkArgument(length.getType().equals(Type.INT_TYPE), "length must have type int");
    
    src.load(this);
    srcPos.load(this);
    dest.load(this);
    destPos.load(this);
    length.load(this);

    invokestatic(System.class, "arraycopy", Type.getMethodDescriptor(Type.VOID_TYPE, 
        Type.getType(Object.class), Type.INT_TYPE,
        Type.getType(Object.class), Type.INT_TYPE,
        Type.INT_TYPE));
    
  }

  public void fillArray(JExpr array, JExpr fromIndex, JExpr toIndex, JExpr value) {
    Preconditions.checkArgument(array.getType().getSort() == Type.ARRAY, "array must have sort ARRAY");
    Preconditions.checkArgument(fromIndex.getType().getSort() == Type.INT, "fromIndex must have type int");
    Preconditions.checkArgument(toIndex.getType().getSort() == Type.INT, "toIndex must have type int");

    int elementSort = array.getType().getElementType().getSort();
    
    array.load(this);
    fromIndex.load(this);
    toIndex.load(this);
    value.load(this);
    
    if(elementSort == Type.OBJECT || elementSort == Type.ARRAY) {
      // General type: Object[], Object
      invokestatic(Arrays.class, "fill", Type.getMethodDescriptor(Type.VOID_TYPE,
          Type.getType(Object[].class),
          Type.INT_TYPE,
          Type.INT_TYPE,
          Type.getType(Object.class)));
    } else {
      // Primitive type, double[], double
      throw new UnsupportedOperationException("TODO");
    }
  }
}
