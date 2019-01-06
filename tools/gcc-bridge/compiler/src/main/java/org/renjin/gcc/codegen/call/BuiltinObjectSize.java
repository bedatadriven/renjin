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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleIntegerType;

/**
 * Built-in construct that returns a constant number of bytes from ptr to the end of the object ptr pointer points to
 * (if known at compile time).
 *
 * __builtin_object_size never evaluates its arguments for side-effects. If there are any side-effects in them,
 * it returns (size_t) -1 for type 0 or 1 and (size_t) 0 for type 2 or 3.
 *
 * If there are multiple objects ptr can point to and all of them are known at compile time, the returned number is
 * the maximum of remaining byte counts in those objects if type & 2 is 0 and minimum if nonzero. If it is not possible
 * to determine which objects ptr points to at compile time, __builtin_object_size should return (size_t) -1 for type 0
 * or 1 and (size_t) 0 for type 2 or 3.
 *
 * type is an integer constant from 0 to 3. If the least significant bit is clear, objects are whole variables, if
 * it is set, a closest surrounding subobject is considered the object a pointer points to. The second bit determines
 * if maximum or minimum of remaining bytes is computed.
 */
public class BuiltinObjectSize implements CallGenerator {

  public static final String NAME = "__builtin_object_size";

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    if(call.getLhs() != null) {
      GimpleExpr object = call.getOperand(0);
      GimpleExpr typeExpr = call.getOperand(1);

      if(!(typeExpr instanceof GimpleIntegerConstant)) {
        throw new InternalCompilerException("Expected constant type argument for " + NAME +
            ", found " + typeExpr.getClass().getSimpleName());
      }

      int type = ((GimpleIntegerConstant) typeExpr).getValue().intValue();

      int result;
      if(type == 0) {
        result = -1;
      } else {
        result = 0;
      }

      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, exprFactory.findGenerator(new GimpleIntegerConstant(new GimpleIntegerType(32), result)));
    }
  }
}
