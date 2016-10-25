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
import org.renjin.compiler.codegen.VariableStorage;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.Collections;
import java.util.List;


public class ReturnStatement implements Statement, BasicBlockEndingStatement {

  private Expression returnValue;

  
  public ReturnStatement(Expression returnValue) {
    super();
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
  public void setRHS(Expression newRHS) {
    this.returnValue = newRHS;
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
  public void setChild(int childIndex, Expression child) {
    assert childIndex == 0;
    returnValue = child;
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitReturn(this);
  }

  @Override
  public int emit(EmitContext emitContext, InstructionAdapter mv) {
    returnValue.load(emitContext, mv);
    mv.areturn(Type.getType(SEXP.class));
    return 0;
  }

  @Override
  public String toString() {
    return "return " + returnValue;
  }
}
