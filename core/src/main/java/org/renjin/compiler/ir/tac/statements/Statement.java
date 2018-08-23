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
package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.commons.InstructionAdapter;


public interface Statement extends TreeNode {

  Iterable<IRLabel> possibleTargets();

  Expression getRHS();

  void setRHS(Expression newRHS);

  void accept(StatementVisitor visitor);

  /**
   * Emits the bytecode for this instruction
   * @param emitContext
   * @param mv
   * @return the required increase to the stack
   */
  int emit(EmitContext emitContext, InstructionAdapter mv);

  /**
   *
   * @return true if this statement has no side effects.
   */
  boolean isPure();
}