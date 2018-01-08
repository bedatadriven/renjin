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
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.List;


public class ArrayValueFunction implements ValueFunction {

  private final GimpleArrayType arrayType;
  private final ValueFunction elementValueFunction;

  public ArrayValueFunction(GimpleArrayType arrayType, ValueFunction elementValueFunction) {
    this.arrayType = arrayType;
    this.elementValueFunction = elementValueFunction;
  }

  @Override
  public Type getValueType() {
    return elementValueFunction.getValueType();
  }

  @Override
  public GimpleType getGimpleValueType() {
    return arrayType;
  }

  @Override
  public int getElementLength() {
    return elementValueFunction.getElementLength() * arrayType.getElementCount();
  }

  @Override
  public int getArrayElementBytes() {
    return elementValueFunction.getArrayElementBytes();
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    return new FatArrayExpr(arrayType, elementValueFunction, arrayType.getElementCount(), array, offset);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new FatArrayExpr(arrayType, elementValueFunction, arrayType.getElementCount(),
        wrapperInstance.getArray(),
        wrapperInstance.getOffset());
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return elementValueFunction.toArrayValues(expr);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, 
                         JExpr valueCount) {

    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    elementValueFunction.memorySet(mv, array, offset, byteValue, length);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return elementValueFunction.getValueConstructor();
  }

  @Override
  public VPtrExpr toVPtr(JExpr array, JExpr offset) {
    return elementValueFunction.toVPtr(array, offset);
  }

  @Override
  public String toString() {
    return "Array[" + elementValueFunction + "]";
  }
}