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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.primitive.op.ConditionExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.statement.GimpleCall;

/**
 * The __builtin_expect() function allows the programmer to provide branch prediction information.
 *
 * <p>Note that this extra information is currently ignored by GCC-Bridge</p>
 */
public class BuiltinExpectGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_expect";
  
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    // Ignore, just load the first argument onto the stack
    GExpr rhs = exprFactory.findGenerator(call.getOperand(0));
    GExpr lhs = exprFactory.findGenerator(call.getLhs());
    lhs.store(mv, rhs);
  }
}
