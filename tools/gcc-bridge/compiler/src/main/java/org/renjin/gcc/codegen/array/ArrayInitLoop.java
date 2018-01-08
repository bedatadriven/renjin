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
import org.renjin.gcc.codegen.expr.ConstantValue;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Allocates a new array and initializes each element with a loop.
 */
public class ArrayInitLoop implements JExpr {

  private ValueFunction valueFunction;
  private int arrayLength;
  private Type arrayType;

  public ArrayInitLoop(ValueFunction valueFunction, int arrayLength) {
    this.valueFunction = valueFunction;
    this.arrayLength = arrayLength;
    this.arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
  }

  @Nonnull
  @Override
  public Type getType() {
    return arrayType;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    JLValue array = mv.getLocalVarAllocator().reserve(arrayType);
    array.store(mv, Expressions.newArray(valueFunction.getValueType(), arrayLength));

    LocalVarAllocator.LocalVar counter = (LocalVarAllocator.LocalVar) mv.getLocalVarAllocator().reserveInt("$counter");
    Label loopHead = new Label();
    Label loopBody = new Label();
    
    // Initialize our loop counter
    counter.store(mv, new ConstantValue(Type.INT_TYPE, 0));
    mv.goTo(loopHead);
    
    // Loop body
    mv.visitLabel(loopBody);
    
    // Assign array element
    array.load(mv);
    counter.load(mv);
    valueFunction.getValueConstructor().get().load(mv);
    mv.astore(valueFunction.getValueType());
    
    mv.iinc(counter.getIndex(), 1);
    
    // Loop head
    mv.visitLabel(loopHead);
    counter.load(mv);
    mv.iconst(arrayLength);
    mv.ificmplt(loopBody);
    
    array.load(mv);
  }
}
