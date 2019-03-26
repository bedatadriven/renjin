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
package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.function.Consumer;


public abstract class Statement implements TreeNode {

  private BasicBlock basicBlock;

  public abstract Iterable<IRLabel> possibleTargets();

  /**
   *
   * @return this statement's "right hand side" expression, or
   * {@link org.renjin.compiler.ir.tac.expressions.NullExpression#INSTANCE} if this
   * statement has no right hand side.
   */
  public abstract Expression getRHS();

  public void forEachVariableUsed(Consumer<LValue> consumer) {
    Expression rhs = getRHS();
    if(rhs instanceof LValue) {
      consumer.accept((LValue)rhs);
    } else {
      for (int i = 0; i < rhs.getChildCount(); i++) {
        Expression child = rhs.childAt(i);
        if(child instanceof LValue) {
          consumer.accept((LValue) child);
        }
      }
    }
  }

  /**
   * Emits the bytecode for this instruction
   */
  public abstract void emit(EmitContext emitContext, InstructionAdapter mv);

  /**
   *
   * @return true if this statement has no side effects.
   */
  public abstract boolean isPure();

  public final BasicBlock getBasicBlock() {
    return basicBlock;
  }

  public final void setBasicBlock(BasicBlock basicBlock) {
    this.basicBlock = basicBlock;
  }
}