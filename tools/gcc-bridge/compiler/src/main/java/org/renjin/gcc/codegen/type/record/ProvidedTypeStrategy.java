/**
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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

public class ProvidedTypeStrategy extends RecordTypeStrategy<GExpr> {

  private final Type jvmType;

  public ProvidedTypeStrategy(GimpleRecordTypeDef gimpleType, Type jvmType) {
    super(gimpleType);
    this.jvmType = jvmType;
  }


  public Type getJvmType() {
    return jvmType;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  private String unsupportedMessage() {
    return "Provided type '" + jvmType.getInternalName() + "' can only be used as a pointer";
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public ValueFunction getValueFunction() {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public GExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public GExpr providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly) {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public GExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new ProvidedPtrStrategy(this);
  }

  @Override
  public TypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

  @Override
  public GExpr cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException {
    throw new UnsupportedOperationException(unsupportedMessage());
  }

}
