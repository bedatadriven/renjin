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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

/**
 * Allocates a 
 */
public final class FatPtrMalloc {

  private static final int MAX_UNROLL_COUNT = 5;
  
  private FatPtrMalloc() {}

  public static FatPtrPair alloc(MethodGenerator mv, ValueFunction valueFunction, JExpr length) {
    return new FatPtrPair(valueFunction, allocArray(mv, valueFunction, length));
  }

  public static JExpr allocArray(MethodGenerator mv, ValueFunction valueFunction, JExpr length) {


    // If the values don't require any initialization (for example, an array of 
    // double is initialized by the JVM to zeros)
    // Then we can just return a new array expression
    if(!valueFunction.getValueConstructor().isPresent()) {
      return Expressions.newArray(valueFunction.getValueType(), length);
    }

    // If we *do* need to construct the array elements, but the length is short and known at compile time,
    // we can unroll it the loop. 

    if(length instanceof ConstantValue) {
      ConstantValue constantLength = (ConstantValue) length;

      if(constantLength.getIntValue() <= MAX_UNROLL_COUNT) {
        List<JExpr> arrayValues = Lists.newArrayList();
        for(int i=0;i<constantLength.getIntValue();++i) {
          arrayValues.add(valueFunction.getValueConstructor().get());
        }
        return Expressions.newArray(valueFunction.getValueType(), arrayValues);
      }
    }

    // Otherwise we need to actually emit a loop to initialize the array at runtime

    // Reserve local variables for the array and for our counter
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    LocalVarAllocator.LocalVar array = mv.getLocalVarAllocator().reserve(arrayType);
    LocalVarAllocator.LocalVar counter = mv.getLocalVarAllocator().reserve(Type.INT_TYPE);


    // First allocate the array
    length.load(mv);
    mv.newarray(valueFunction.getValueType());
    mv.store(array.getIndex(), arrayType);

    // initialize the loop counter
    mv.iconst(0);
    mv.store(counter.getIndex(), Type.INT_TYPE);

    // Now loop until we've initialized all array elements
    Label loopHead = new Label();
    Label loopCheck = new Label();

    mv.goTo(loopCheck);

    // Initialize the values
    mv.mark(loopHead);
    array.load(mv);
    counter.load(mv);
    valueFunction.getValueConstructor().get().load(mv);
    mv.astore(valueFunction.getValueType());
    mv.iinc(counter.getIndex(), 1);

    // Check the condition
    mv.mark(loopCheck);
    mv.load(counter.getIndex(), Type.INT_TYPE);
    length.load(mv);
    mv.ificmplt(loopHead);

    // Load the array back on the stack
    return array;
  }

}
