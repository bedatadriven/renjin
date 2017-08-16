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
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePointerType;


/**
 * Generates calls to memcmp() depending on the type of the arguments
 */
public class MemCmpCallGenerator implements CallGenerator {

  private final TypeOracle typeOracle;

  public MemCmpCallGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    if(call.getLhs() != null) {
      GExpr p1 = exprFactory.findGenerator(call.getOperand(0));
      GExpr p2 = exprFactory.findGenerator(call.getOperand(1));
      JExpr n = exprFactory.findPrimitiveGenerator(call.getOperand(2));

      GimplePointerType type = (GimplePointerType) call.getOperand(0).getType();
      JExpr result = typeOracle.forPointerType(type).memoryCompare(mv, p1, p2, n);

      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, new PrimitiveValue(new GimpleIntegerType(32), result));
    }

  }
}
