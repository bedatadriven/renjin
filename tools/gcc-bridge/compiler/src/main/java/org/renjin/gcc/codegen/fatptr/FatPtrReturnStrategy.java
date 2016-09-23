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
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for returning fat pointers from methods.  
 * 
 * <p>We cannot unfortunately return both the array and offset from a JVM method, so they need to
 * be wrapped in a xxxPtr object.
 */
public class FatPtrReturnStrategy implements ReturnStrategy {

  private ValueFunction valueFunction;

  public FatPtrReturnStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  @Override
  public Type getType() {
    return Wrappers.wrapperType(valueFunction.getValueType());
  }

  @Override
  public JExpr marshall(GExpr expr) {
    FatPtrPair fatPtr = (FatPtrPair) expr;
    return fatPtr.wrap();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    // Store the returned Ptr wrapper to a local variable
    JLValue wrapper = mv.getLocalVarAllocator().reserve(returnValue.getType());
    wrapper.store(mv, returnValue);

    JExpr array = Wrappers.arrayField(wrapper, valueFunction.getValueType());
    JExpr offset = Wrappers.offsetField(wrapper);

    return new FatPtrPair(valueFunction, array, offset);
  }

  @Override
  public JExpr getDefaultReturnValue() {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    return new FatPtrPair(valueFunction, Expressions.nullRef(arrayType)).wrap();
  }
}
