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
package org.renjin.gcc.codegen.type.voidt;

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
import org.renjin.gcc.gimple.type.GimpleVoidType;
import org.renjin.repackaged.asm.Type;

/**
 * Creates generators for void value types. Only used for return types.
 */
public class VoidTypeStrategy implements TypeStrategy<GExpr> {

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VoidReturnStrategy();
  }

  @Override
  public ValueFunction getValueFunction() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException("parameters cannot have 'void' type");
  }

  @Override
  public GExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    throw new UnsupportedOperationException("variables cannot have 'void' type");
  }

  @Override
  public GExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    throw new UnsupportedOperationException("variables cannot have 'void' type");
  }

  @Override
  public GExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException("constructors cannot have 'void' type");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("fields cannot have 'void' type");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("fields cannot have 'void' type");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VPtrStrategy(new GimpleVoidType());
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("arrays cannot have component type of 'void'");
  }

  @Override
  public GExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }

}
