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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;


public interface PointerTypeStrategy<ExprT extends GExpr> extends TypeStrategy<ExprT> {
  
  ExprT malloc(MethodGenerator mv, JExpr sizeInBytes);

  ExprT realloc(MethodGenerator mv, ExprT pointer, JExpr newSizeInBytes);

  ExprT pointerPlus(MethodGenerator mv, ExprT pointer, JExpr offsetInBytes);

  ExprT nullPointer();

  ConditionGenerator comparePointers(MethodGenerator mv, GimpleOp op, ExprT x, ExprT y);

  void memoryCopy(MethodGenerator mv, ExprT destination, ExprT source, JExpr length, boolean buffer);

  void memorySet(MethodGenerator mv, ExprT pointer, JExpr byteValue, JExpr length);

  ExprT unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer);

}
