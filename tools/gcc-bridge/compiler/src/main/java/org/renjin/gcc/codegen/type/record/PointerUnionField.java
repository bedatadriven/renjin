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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.*;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

/**
 * A field that is the union of more than one pointer types.
 * We store the value as a single Object instance.
 */
public class PointerUnionField extends SingleFieldStrategy {

  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  
  public PointerUnionField(Type declaringClass, String fieldName) {
    super(declaringClass, fieldName, OBJECT_TYPE);
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new UnsupportedOperationException("TODO: offset = " + offset);
    }
    
    JLValue fieldExpr = Expressions.field(instance, Type.getType(Object.class), fieldName);

    if(expectedType == null) {
      return new VoidPtr(fieldExpr);
    }

    if(expectedType instanceof FatPtrStrategy) {
      return new FatPtrMemberExpr(fieldExpr, expectedType.getValueFunction());
    } 
    throw new UnsupportedOperationException(String.format("TODO: strategy = %s", expectedType));
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    memsetReference(mv, instance, byteValue, byteCount);
  }

  private class FatPtrMemberExpr implements FatPtr {

    private JLValue fieldExpr;
    private ValueFunction valueFunction;

    public FatPtrMemberExpr(JLValue fieldExpr, ValueFunction valueFunction) {
      this.fieldExpr = fieldExpr;
      this.valueFunction = valueFunction;
    }

    @Override
    public Type getValueType() {
      return valueFunction.getValueType();
    }

    @Override
    public boolean isAddressable() {
      return false;
    }

    @Override
    public JExpr wrap() {
      return fieldExpr;
    }

    @Override
    public FatPtrPair toPair(MethodGenerator mv) {
      Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
      JExpr wrapper = Expressions.cast(fieldExpr, wrapperType);

      return Wrappers.toPair(mv, valueFunction, wrapper);
    }

    @Override
    public void store(MethodGenerator mv, GExpr rhs) {
      if(rhs instanceof FatPtr) {
        fieldExpr.store(mv, ((FatPtr) rhs).wrap());
      } else {
        throw new UnsupportedOperationException("TODO: " + rhs.getClass().getName());
      }
    }

    @Override
    public GExpr addressOf() {
      throw new NotAddressableException();
    }

    @Override
    public void jumpIfNull(MethodGenerator mv, Label label) {
      throw new UnsupportedOperationException("TODO");
    }

    @Override
    public GExpr valueOf() {
      throw new UnsupportedOperationException("TODO");
    }
  }
  
}
