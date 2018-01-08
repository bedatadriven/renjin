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
package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.statement.GimpleCall;

/**
 * Generates bytecode for calls to memset()
 */
public class MemSetGenerator implements CallGenerator {

  /**
   * We use this implementation also for __memset_chk, which includes
   * buffer overflow checking because the JVM does this anyway.
   */
  public static final String MEMSET_CHECK_BUILTIN = "__memset_chk";

  private final TypeOracle typeOracle;

  public MemSetGenerator(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {

    
    PtrExpr pointer = (PtrExpr) exprFactory.findGenerator(call.getOperand(0));
    JExpr byteValue = exprFactory.findPrimitiveGenerator(call.getOperand(1));
    JExpr length = exprFactory.findPrimitiveGenerator(call.getOperand(2));

    pointer.memorySet(mv, byteValue, length);

    if(call.getLhs() != null) {
      GExpr lhs = exprFactory.findGenerator(call.getLhs());
      lhs.store(mv, pointer);
    }
  }
}
