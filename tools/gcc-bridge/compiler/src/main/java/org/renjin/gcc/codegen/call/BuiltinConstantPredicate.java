/*
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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleIntegerType;

/**
 * ou can use the built-in function __builtin_constant_p to determine if a value is known to be constant at compile
 * time and hence that GCC can perform constant-folding on expressions involving that value. The argument of the
 * function is the value to test. The function returns the integer 1 if the argument is known to be a compile-time
 * constant and 0 if it is not known to be a compile-time constant. A return of 0 does not indicate that the value
 * is not a constant, but merely that GCC cannot prove it is a constant with the specified value of the -O option.
 */
public class BuiltinConstantPredicate implements CallGenerator {

  public static final String NAME = "__builtin_constant_p";

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    if(call.getLhs() != null) {
      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      GimpleExpr argument = call.getOperand(0);
      int resultValue;
      if (argument instanceof GimpleConstant) {
        resultValue = 1;
      } else {
        resultValue = 0;
      }
      lhs.store(mv, exprFactory.findGenerator(new GimpleIntegerConstant(new GimpleIntegerType(32), resultValue)));
    }
  }
}
