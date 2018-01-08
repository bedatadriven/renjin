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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.asm.Type;

/**
 *  Provides strategies for code generation for a specific {@code GimpleType}
 *  
 */
public interface TypeStrategy<ExprT extends GExpr> {

  /**
   * 
   * @return the {@link ParamStrategy} for this type.
   */
  ParamStrategy getParamStrategy();

  /**
   * @return the {@link ReturnStrategy} for this type.
   */
  ReturnStrategy getReturnStrategy();

  ValueFunction getValueFunction();

  /**
   * Creates an expression generator for {@link GimpleVarDecl}s of this type
   */
  ExprT variable(GimpleVarDecl decl, VarAllocator allocator);


  /**
   * Provides an expression generator for a global variable of this type which is defined
   * in an external Java field.
   */
  ExprT providedGlobalVariable(GimpleVarDecl decl, JExpr expr, boolean readOnly);

  /**
   * Creates an expression generator for constructors of this type.
   */
  ExprT constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value);

  /**
   * Creates a new FieldGenerator for fields of this type.
   *
   * @param className the full internal name of the class, for example, "org/renjin/gcc/Record$1"
   * @param fieldName the name of the field
   */
  FieldStrategy fieldGenerator(Type className, String fieldName);


  FieldStrategy addressableFieldGenerator(Type className, String fieldName);

  /**
   * @return a {@code PointerTypeStrategy} for pointers of this type
   */
  PointerTypeStrategy pointerTo();

  /**
   * @param arrayType 
   * @return a strategy for arrays of this type
   */
  TypeStrategy arrayOf(GimpleArrayType arrayType);

  /**
   * 
   * Casts the given {@code value}, compield with the given {@code typeStrategy}, 
   * to a value of this strategy.
   */
  ExprT cast(MethodGenerator mv, GExpr value) throws UnsupportedCastException;

}
