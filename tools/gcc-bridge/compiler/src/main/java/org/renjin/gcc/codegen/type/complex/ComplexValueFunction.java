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
package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.*;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;
import java.util.Optional;

public class ComplexValueFunction implements ValueFunction {
  
  private final GimpleComplexType valueType;

  public ComplexValueFunction(GimpleComplexType valueType) {
    this.valueType = valueType;
  }

  @Override
  public Type getValueType() {
    return valueType.getJvmPartType();
  }

  @Override
  public GimpleType getGimpleValueType() {
    return valueType;
  }

  @Override
  public int getElementLength() {
    return 2;
  }

  @Override
  public int getArrayElementBytes() {
    return valueType.sizeOf() / 2;
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {

    FatPtrPair address = new FatPtrPair(this, array, offset);
    return dereference(array, offset, address);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return dereference(wrapperInstance.getArray(), wrapperInstance.getOffset(), wrapperInstance);
  }

  private GExpr dereference(JExpr array, JExpr offset, FatPtr address) {
    // Real element is at i
    JExpr realOffset = offset;
    // Complex element is at i+1
    JExpr imaginaryOffset = Expressions.sum(realOffset, Expressions.constantInt(1));

    JExpr real = Expressions.elementAt(array, realOffset);
    JExpr imaginary = Expressions.elementAt(array, imaginaryOffset);

    return new ComplexExpr(address, real, imaginary);
  }


  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    ComplexExpr value = (ComplexExpr) expr;
    return Lists.newArrayList(value.getRealJExpr(), value.getImaginaryJExpr());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, 
                         JExpr valueCount) {
    
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, 
        Expressions.product(valueCount, 2));
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    Memset.primitiveMemset(mv, valueType.getJvmPartType(), array, offset, byteValue, length);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.empty();
  }

  @Override
  public VPtrExpr toVPtr(JExpr array, JExpr offset) {
    throw new UnsupportedOperationException("TODO");
  }


  @Override
  public String toString() {
    return "Complex[" + valueType + "]";
  }
}
