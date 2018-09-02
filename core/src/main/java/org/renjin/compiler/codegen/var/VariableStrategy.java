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
package org.renjin.compiler.codegen.var;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

/**
 * Strategy for storing SEXP variables on the stack
 */
public abstract class VariableStrategy {


  public abstract CompiledSexp getCompiledExpr();

  public abstract void store(EmitContext emitContext, InstructionAdapter mv, CompiledSexp compiledSexp);


  /**
   * @return true if this variable is "live out" at the given {@code statement}. In other words, can we
   * mutate it here without interfering with subsequent uses?
   */
  public boolean isLiveOut(Statement statement) {
    return true;
  }
}
