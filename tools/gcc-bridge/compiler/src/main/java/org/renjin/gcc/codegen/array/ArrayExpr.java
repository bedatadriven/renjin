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
package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.codegen.type.fun.FunPtr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;


public class ArrayExpr implements GExpr {
  
  private JExpr array;
  private JExpr offset;
  private ValueFunction valueFunction;
  private int length;
  
  public ArrayExpr(ValueFunction valueFunction, int length, JExpr array) {
    this.array = array;
    this.valueFunction = valueFunction;
    this.length = length;
    this.offset = Expressions.zero();
  }

  public ArrayExpr(ValueFunction valueFunction, int length, JExpr array, JExpr offset) {
    this.valueFunction = valueFunction;
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  public ValueFunction getValueFunction() {
    return valueFunction;
  }

  public JExpr getArray() {
    return array;
  }

  public JExpr getOffset() {
    return offset;
  }
  
  public GExpr first() {
    return valueFunction.dereference(array, offset);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ArrayExpr rhsExpr = (ArrayExpr) rhs;
    int copyLength = Math.min(rhsExpr.length, length);
    
    valueFunction.memoryCopy(mv, 
        array, offset, 
        rhsExpr.getArray(), rhsExpr.getOffset(), 
        Expressions.constantInt(copyLength));
  }

  @Override
  public GExpr addressOf() {
    return new FatPtrPair(valueFunction, array, offset);
  }

  @Override
  public FunPtr toFunPtr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public ArrayExpr toArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  @Override
  public PrimitiveValue toPrimitiveExpr(GimplePrimitiveType targetType) throws UnsupportedCastException {
    return first().toPrimitiveExpr(targetType);
  }

  @Override
  public VoidPtr toVoidPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RecordArrayExpr toRecordArrayExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VPtrExpr toVPtrExpr() throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

}
