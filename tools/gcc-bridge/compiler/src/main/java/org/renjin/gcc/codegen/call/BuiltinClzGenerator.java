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

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.repackaged.guava.base.Preconditions;

/**
 * Count leading zeros of an integer
 */
public class BuiltinClzGenerator implements CallGenerator {
  
  public static final String NAME = "__builtin_clz";
  
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    Preconditions.checkArgument(call.getOperands().size() == 1, "Expected 1 argument");
    
    // If we are not assigning the result, this is is a NO-OP
    if(call.getLhs() == null) {
      return;
    }
    GExpr lhs = exprFactory.findGenerator(call.getLhs());
    
    
    GExpr value = exprFactory.findGenerator(call.getOperand(0));
    if(!(value instanceof PrimitiveValue)) {
      throw new InternalCompilerException("Expected primitive operand: " + value);
    }
    
    PrimitiveValue argument = (PrimitiveValue) value;
    PrimitiveValue result = new PrimitiveValue(
        new GimpleIntegerType(32),
        Expressions.numberOfLeadingZeros(argument.unwrap()));
    
    lhs.store(mv, result);
  }
}
