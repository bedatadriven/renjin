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

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.var.ConstantVar;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.IRFormatting;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Collections;


public class Assignment extends Statement {
  private LValue lhs;
  private Expression rhs;
 
  public Assignment(LValue lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
 
  public LValue getLHS() {
    return lhs;
  }
 
  @Override
  public Expression getRHS() {
    return rhs;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public String toString() {
    return getLHS() + " " + IRFormatting.LEFT_ARROW + " "  + rhs;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return rhs;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      rhs = child;
    } else {
      throw new IllegalArgumentException("childIndex=" + childIndex);
    }
  }

  @Override
  public void emit(EmitContext emitContext, InstructionAdapter mv) {


    VariableStrategy lhsExpr = emitContext.getVariable(lhs);
    if(lhsExpr == null || lhsExpr instanceof ConstantVar) {
      // LHS never used or constant, no need to generate bytecode
      return;
    }

    rhs.emitAssignment(emitContext, mv, this);
  }

  @Override
  public boolean isPure() {
    return rhs.isPure();
  }

  public void setLHS(LValue lhs) {
    this.lhs = lhs;
  }
}
