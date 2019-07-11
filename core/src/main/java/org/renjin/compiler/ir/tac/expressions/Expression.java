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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.repackaged.asm.commons.InstructionAdapter;


public interface Expression extends TreeNode {


  /**
   *
   * @return true if we are absolutely certain this expression has no side effects
   */
  boolean isPure();

  /**
   * Resolves and stores the type of this Expression, based on it's
   * child nodes
   * @param typeMap
   */
  ValueBounds updateTypeBounds(ValueBoundsMap typeMap);

  /**
   * @return this expression's current value bounds after the last call to {@code updateTypeBounds}
   */
  ValueBounds getValueBounds();


  CompiledSexp getCompiledExpr(EmitContext emitContext);

  /**
   * Emits the bytecode to assign this expression to the given lhs.
   *
   */
  default void emitAssignment(EmitContext emitContext, InstructionAdapter mv, Assignment statement) {
    VariableStrategy lhs = emitContext.getVariable(statement.getLHS());
    CompiledSexp rhs = statement.getRHS().getCompiledExpr(emitContext);

    lhs.store(emitContext, mv, rhs);
  }

  /**
   * Emits the bytecode to execute this expression for side effects.
   */
  default void emitExecute(EmitContext emitContext, InstructionAdapter mv) {
    if(!isPure()) {
      throw new IllegalStateException("Missing implementation for " + getClass().getName());
    }
  }
}
