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
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;

/**
 * Strategy for dealing with primitive types.
 * 
 * <p>This is the easiest case, because there is (mostly) a one-to-one correspondence between primitive
 * types in {@code Gimple} and those of the JVM.</p>
 */
public class PrimitiveTypeStrategy implements TypeStrategy<PrimitiveExpr> {
  
  private PrimitiveType type;

  public PrimitiveTypeStrategy(GimplePrimitiveType type) {
    this.type = PrimitiveType.of(type);
  }

  public GimplePrimitiveType getType() {
    return type.gimpleType();
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new PrimitiveParamStrategy(type);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new PrimitiveReturnStrategy(type);
  }

  public ValueFunction getValueFunction() {
    return valueFunction();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, valueFunction());
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new PrimitiveFieldStrategy(className, fieldName, type);
  }

  @Override
  public PrimitiveExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      JLValue unitArray = allocator.reserveUnitArray(decl.getNameIfPresent(), type.localVariableType(), Optional.<JExpr>absent());
      FatPtrPair address = new FatPtrPair(valueFunction(), unitArray);
      JExpr value = Expressions.elementAt(address.getArray(), 0);
      return type.fromStackValue(value, address);
      
    } else {
      return type.fromStackValue(allocator.reserve(decl.getNameIfPresent(), type.localVariableType()));
    }
  }

  @Override
  public PrimitiveExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    Type javaType = expr.getType();
    if(!javaType.equals(this.type.jvmType())) {
      throw new UnsupportedOperationException("Cannot map global variable " + decl + " to JVM field of type " + expr + ". " +
          "Expected static field of type " + this.type.jvmType());
    }

    PtrExpr address = null;
    if(readOnly) {
      address = new FatPtrPair(valueFunction(), Expressions.newArray(type.jvmType(), Collections.singletonList(expr)));
    }
    return type.fromStackValue(expr, address);
  }

  @Override
  public PrimitiveExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VPtrStrategy(type.gimpleType());
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, valueFunction());
  }

  @Override
  public PrimitiveExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    return value.toPrimitiveExpr();
  }

  public PrimitiveExpr zero() {
    return type.fromStackValue(new ConstantValue(type.jvmType(), 0));
  }

  private PrimitiveValueFunction valueFunction() {
    return new PrimitiveValueFunction(type);
  }

  @Override
  public String toString() {
    return "PrimitiveTypeStrategy[" + type + "]";
  }

  public Type getJvmType() {
    return type.jvmType();
  }


}
