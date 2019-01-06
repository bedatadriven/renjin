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
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleCall;

import java.util.Optional;

public class VarArgsStartGenerator implements CallGenerator {

  public static final String NAME = "__builtin_va_start";

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    GimpleAddressOf stateVar = (GimpleAddressOf) call.getOperand(0);
    GimpleExpr lastFixedVarIndex = call.getOperand(1);

    Optional<VPtrExpr> varArgsPtr = exprFactory.getVarArgsPtr();
    if(!varArgsPtr.isPresent()) {
      throw new IllegalStateException(NAME + " called in non-variadic function!");
    }

    GExpr receiver = exprFactory.findGenerator(stateVar.getValue());
    receiver.store(mv, varArgsPtr.get());
  }
}
