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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.Memset;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.WrappedFatPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.RecordUnitPtrPtr;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProvidedPtrValueFunction implements ValueFunction {

  private GimpleRecordType gimpleType;
  private Type jvmType;


  public ProvidedPtrValueFunction(ProvidedTypeStrategy strategy) {
    this.gimpleType = strategy.getGimpleType();
    this.jvmType = strategy.getJvmType();
  }

  @Override
  public Type getValueType() {
    return jvmType;
  }

  @Override
  public GimpleType getGimpleValueType() {
    return gimpleType;
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getArrayElementBytes() {
    return 4;
  }

  @Override
  public Optional<JExpr> getValueConstructor() {
    return Optional.empty();
  }

  @Override
  public VPtrExpr toVPtr(JExpr array, JExpr offset) {
    return new VPtrExpr(Expressions.newObject(Type.getType(RecordUnitPtrPtr.class),
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class), Type.INT_TYPE),
        array,
        offset));
  }

  @Override
  public GExpr dereference(JExpr array, JExpr offset) {
    JExpr pointerValue = Expressions.elementAt(array, offset);
    JExpr castedPointerValue = Expressions.cast(pointerValue, jvmType);
    FatPtrPair pointerAddress = new FatPtrPair(this, array, offset);
    
    return new ProvidedPtrExpr(castedPointerValue, pointerAddress);
  }

  @Override
  public GExpr dereference(WrappedFatPtrExpr wrapperInstance) {
    return new ProvidedPtrExpr(wrapperInstance.valueExpr(), wrapperInstance);
  }

  @Override
  public List<JExpr> toArrayValues(GExpr expr) {
    return Collections.singletonList((JExpr)expr);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, JExpr destinationArray, JExpr destinationOffset, JExpr sourceArray, JExpr sourceOffset, JExpr valueCount) {
    mv.arrayCopy(sourceArray, sourceOffset, destinationArray, destinationOffset, valueCount);
  }

  @Override
  public void memorySet(MethodGenerator mv, JExpr array, JExpr offset, JExpr byteValue, JExpr length) {
    Memset.zeroOutRefArray(mv, array, offset, length);
  }

  @Override
  public String toString() {
    return "RecordUnitPtr[" + jvmType + "]";
  }
}
