/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.repackaged.asm.Type;

import java.util.Arrays;

public class Memset {

  /**
   * memset() for an array of primitive values
   */
  public static void primitiveMemset(MethodGenerator mv, Type valueType,
                                     JExpr array,
                                     JExpr offset,
                                     JExpr byteValue, JExpr length) {
        
    // Each of the XXXPtr classes have a static memset() method in the form:
    // void memset(double[] str, int strOffset, int c, int n)

    Type wrapperType = Wrappers.wrapperType(valueType);
    Type arrayType = Wrappers.valueArrayType(valueType);

    String methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, arrayType, 
        Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE);

    array.load(mv);
    offset.load(mv);
    byteValue.load(mv);
    length.load(mv);
    
    mv.invokestatic(wrapperType, "memset", methodDescriptor);
  }
 
  public static void zeroOutRefArray(MethodGenerator mv, JExpr array, JExpr offset, JExpr length) {
    // Since this call to memset() can never result in a valid pointer, we assume that 
    // all garbage pointers will produce the same result and so we choose NULL as our garbage pointer

    JExpr elementCount = Expressions.divide(length, GimplePointerType.SIZE / 8);
    JExpr toIndex = Expressions.sum(offset, elementCount);
    
    // fill(Object[] a, int fromIndex, int toIndex, Object val)
    array.load(mv);
    offset.load(mv);
    toIndex.load(mv);
    mv.aconst(null);
    mv.invokestatic(Arrays.class, "fill", Type.getMethodDescriptor(Type.VOID_TYPE, 
        Type.getType(Object[].class), 
        Type.INT_TYPE, 
        Type.INT_TYPE, 
        Type.getType(Object.class)));
  }
  
}
