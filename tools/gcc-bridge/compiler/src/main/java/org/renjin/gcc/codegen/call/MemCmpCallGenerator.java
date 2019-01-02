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

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.primitive.PrimitiveType;
import org.renjin.gcc.gimple.statement.GimpleCall;


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
      PtrExpr p1 = (PtrExpr) exprFactory.findGenerator(call.getOperand(0));
      PtrExpr p2 = (PtrExpr) exprFactory.findGenerator(call.getOperand(1));
      JExpr n = exprFactory.findPrimitiveGenerator(call.getOperand(2));

      JExpr result = p1.memoryCompare(mv, p2, n);

      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, PrimitiveType.INT32.fromStackValue(result));
    }

  }
}
