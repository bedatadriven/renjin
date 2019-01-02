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
package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.repackaged.asm.Type;

/**
 * Creates {@code Generators} for values for function values.
 * 
 * <p>Function pointers are compiled to {@link java.lang.invoke.MethodHandle}s, but since Gimple
 * is statically typed, we don't need the {@code invokedynamic} bytecode and can simply use
 * {@link java.lang.invoke.MethodHandle#invokeExact(Object...)} to invoke function calls.</p>
 */ 
public class FunTypeStrategy implements TypeStrategy<FunExpr> {

  private GimpleFunctionType type;

  public FunTypeStrategy(GimpleFunctionType type) {
    this.type = type;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw newInvalidOperation();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    throw newInvalidOperation();
  }

  @Override
  public ValueFunction getValueFunction() {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    throw newInvalidOperation();
  }

  @Override
  public FunExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    throw newInvalidOperation();
  }

  @Override
  public FunExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw newInvalidOperation();
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw newInvalidOperation();
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw newInvalidOperation();
  }
  
  @Override
  public PointerTypeStrategy pointerTo() {
    return new FunPtrStrategy();
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw newInvalidOperation();
  }

  @Override
  public FunExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

  private UnsupportedOperationException newInvalidOperation() {
    return new UnsupportedOperationException("Invalid operation for function value type. " +
        "(Should this be a function *pointer* instead?");
  }


}
