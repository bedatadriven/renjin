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
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Collections;


public class ReturnStatement extends Statement implements BasicBlockEndingStatement {

  private Expression returnValue;

  public ReturnStatement(Expression returnValue) {
    this.returnValue = returnValue;
  }

  public Expression getReturnValue() {
    return returnValue;
  }


  @Override
  public Expression getRHS() {
    return returnValue;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    assert index == 0;
    return returnValue;
  }

  @Override
  public void setChild(int index, Expression child) {
    assert index == 0;
    returnValue = child;
  }

  @Override
  public void emit(EmitContext emitContext, InstructionAdapter mv) {
    CompiledSexp compiledReturnValue = returnValue.getCompiledExpr(emitContext);
    emitContext.writeReturn(mv, compiledReturnValue);
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("return ");
    s.append(returnValue);
    return s.toString();
  }
}
