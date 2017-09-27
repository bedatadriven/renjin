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
package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for complex number types.
 * 
 * <p>The JVM does not have builtin support for complex number types, so we have to
 * be a bit creative in representing parameters and variables.</p>
 *
 * <p>When used purely as value types within a function, we can simply store the complex
 * values as two {@code double} values on the stack, which should be perfectly equivalent
 * to how GCC treats them.</p>
 * 
 * <p>If we need their address, however, then they need to be allocated on the heap as a double array.</p>
 * 
 * <p>We also can't return double values, so we map functions return a complex value to one
 * returning a double array. This does require the extra step of allocating a double[] array in order
 * to return a complex value. This is something we could eliminate with aggressive inlining.</p>
 * 
 */
public class ComplexTypeStrategy implements TypeStrategy<ComplexValue> {

  private GimpleComplexType type;

  public ComplexTypeStrategy(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public ComplexValue variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
//      return new AddressableComplexVarGenerator(type,
//          allocator.reserveArrayRef(decl.getName(), type.getJvmPartType()));
      throw new UnsupportedOperationException("TODO");
    } else {
      return new ComplexValue(
          allocator.reserve(decl.getName() + "$real", type.getJvmPartType()),
          allocator.reserve(decl.getName() + "$im", type.getJvmPartType()));
    }
  }

  @Override
  public ComplexValue providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ComplexValue constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new ComplexReturnStrategy(type);
  }

  @Override
  public ValueFunction getValueFunction() {
    return new ComplexValueFunction(type);
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VPtrStrategy(type);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new ComplexValueFunction(type))
        .setParameterWrapped(false);
  }

  @Override
  public ComplexValue cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }
}
