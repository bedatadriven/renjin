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
package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Collections;


/**
 * Statement that is evaluated for side-effects
 */
public class ExprStatement implements Statement {

  private Expression operand;
  
  public ExprStatement(Expression operand) {
    super();
    this.operand = operand;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  
  @Override
  public Expression getRHS() {
    return operand;
  }

  @Override
  public String toString() {
    return operand.toString();
  }

  @Override
  public void setRHS(Expression newRHS) {
    this.operand = newRHS;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return operand;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      operand = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitExprStatement(this);
  }

  @Override
  public int emit(EmitContext emitContext, InstructionAdapter mv) {
    if(!operand.isPure()) {
      int stackSizeIncrease = operand.load(emitContext, mv);
      mv.visitInsn(Opcodes.POP);
      return stackSizeIncrease;
    } else {
      return 0;
    }
  }

  @Override
  public boolean isPure() {
    return getRHS().isPure();
  }
}
