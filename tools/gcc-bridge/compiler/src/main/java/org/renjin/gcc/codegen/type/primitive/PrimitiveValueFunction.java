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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.Memset;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;


public class PrimitiveValueFunction implements ValueFunction {

  private GimplePrimitiveType gimpleType;
  private Type type;
  private int byteSize;

  public PrimitiveValueFunction(GimplePrimitiveType type) {
    this.gimpleType = type;
    this.type = type.jvmType();
    this.byteSize = type.sizeOf();
  }
  
  public PrimitiveValueFunction(Type type) {
    this(GimplePrimitiveType.fromJvmType(type));
  }

  public GimplePrimitiveType getGimpleType() {
    return gimpleType;
  }

  @Override
  public Type getValueType() {
    return type;
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return byteSize;
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    FatPtrPair address = new FatPtrPair(this, array, offset);
    JExpr value = Expressions.elementAt(array, offset);

    return new PrimitiveValue(value, address);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new PrimitiveValue(wrapperInstance.valueExpr(), wrapperInstance);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    PrimitiveValue primitiveValue = (PrimitiveValue) expr;
    return Collections.singletonList(primitiveValue.getExpr());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {

    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    Memset.primitiveMemset(mv, type, array, offset, byteValue, length);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.absent();
  }

  @Override
  public String toString() {
    return "Primitive[" + type + "]";
  }
}
