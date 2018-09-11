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
package org.renjin.compiler.builtins;


import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

public interface Specialization {

  ValueBounds getResultBounds();

  /**
   *
   * @return true if the specialized operation is known to be free of side effects.
   */
  boolean isPure();

  CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments);

  default void emitAssignment(EmitContext emitContext, InstructionAdapter mv, Assignment statement, List<IRArgument> arguments) {
    VariableStrategy lhs = emitContext.getVariable(statement.getLHS());
    CompiledSexp rhs = getCompiledExpr(emitContext, arguments);
    lhs.store(emitContext, mv, rhs);
  }
}
