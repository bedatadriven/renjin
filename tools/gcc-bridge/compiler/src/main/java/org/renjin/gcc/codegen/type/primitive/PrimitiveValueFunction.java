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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.Memset;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.codegen.vptr.PointerType;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.Double96Ptr;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class PrimitiveValueFunction implements ValueFunction {

  private PrimitiveType type;
  private int byteSize;

  public PrimitiveValueFunction(PrimitiveType type) {
    this.type = type;
    this.byteSize = type.gimpleType().sizeOf();
  }

  public PrimitiveValueFunction(GimplePrimitiveType primitiveType) {
    this(PrimitiveType.of(primitiveType));
  }

  @Override
  public Type getValueType() {
    return type.jvmType();
  }

  @Override
  public GimpleType getGimpleValueType() {
    return type.gimpleType();
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

    return type.fromNonStackValue(value, address);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return type.fromNonStackValue(wrapperInstance.valueExpr(), wrapperInstance);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    PrimitiveExpr primitiveExpr = (PrimitiveExpr) expr;
    return Collections.singletonList(primitiveExpr.jexpr());
  }

  @Override
  public void memoryCopy(MethodGenerator mv, 
                         JExpr destinationArray, JExpr destinationOffset, 
                         JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {

    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    Memset.primitiveMemset(mv, type.jvmType(), array, offset, byteValue, length);
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.empty();
  }

  @Override
  public VPtrExpr toVPtr(JExpr array, JExpr offset) {

    PointerType pointerType = PointerType.ofPrimitiveType(type.gimpleType());

    // Special handling for double[] -> Real96
    if(pointerType == PointerType.REAL96 && array.getType().getElementType().equals(Type.DOUBLE_TYPE)) {
      JExpr newWrapper = Expressions.newObject(Type.getType(Double96Ptr.class), array, offset);
      return new VPtrExpr(newWrapper);
    }

    JExpr newWrapper = Expressions.newObject(pointerType.alignedImpl(), array, offset);
    return new VPtrExpr(newWrapper);
  }

  @Override
  public String toString() {
    return "Primitive[" + type + "]";
  }
}
