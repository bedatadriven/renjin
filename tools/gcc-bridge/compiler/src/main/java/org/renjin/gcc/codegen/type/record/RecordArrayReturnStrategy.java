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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

/**
 * Strategy for returning record values represented by arrays.
 * 
 * <p>In C, returning a {@code struct} value, as opposed to </p>
 * 
 */
public class RecordArrayReturnStrategy implements ReturnStrategy {

  private RecordArrayValueFunction valueFunction;
  private Type arrayType;
  private int arrayLength;

  public RecordArrayReturnStrategy(RecordArrayValueFunction valueFunction, Type arrayType, int arrayLength) {
    this.valueFunction = valueFunction;
    Preconditions.checkArgument(arrayType.getSort() == Type.ARRAY, "Not an array type: " + arrayType);
    this.arrayType = arrayType;
    this.arrayLength = arrayLength;
  }

  @Override
  public Type getType() {
    return arrayType;
  }

  public Type getArrayComponentType() {
    // array type is [B for example,
    // so strip [
    String descriptor = arrayType.getDescriptor();
    return Type.getType(descriptor.substring(1));
  }

  /**
   * Returns an expression representing a copy of the given array value.
   *
   */
  @Override
  public JExpr marshall(GExpr value) {
    RecordArrayExpr arrayValue = (RecordArrayExpr) value;
    return arrayValue.copyArray();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    return new RecordArrayExpr(valueFunction, returnValue, arrayLength);
  }

  @Override
  public GExpr unmarshall(JExpr returnValue) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public JExpr getDefaultReturnValue() {
    return Expressions.newArray(getArrayComponentType(), arrayLength);
  }
}
