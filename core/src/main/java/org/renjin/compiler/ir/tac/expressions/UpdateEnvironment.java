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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Updates the evaluation environment with the given value
 */
public class UpdateEnvironment extends Statement {

  private Symbol name;
  private Expression rhs;

  public UpdateEnvironment(Symbol name, EnvironmentVariable var) {
    this.name = name;
    this.rhs = var;
  }


  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public Expression getRHS() {
    return rhs;
  }

  @Override
  public void emit(EmitContext emitContext, InstructionAdapter mv) {
    mv.load(emitContext.getEnvironmentVarIndex(), Type.getType(Environment.class));
    mv.aconst(name.getPrintName());

    CompiledSexp compiledExpr = rhs.getCompiledExpr(emitContext);
    compiledExpr.loadSexp(emitContext, mv);

    mv.invokevirtual(Type.getInternalName(Environment.class), "setVariableUnsafe",
          Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(SEXP.class)), false);
  }

  @Override
  public void forEachVariableUsed(Consumer<LValue> consumer) {
    consumer.accept((LValue)rhs);
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    assert  childIndex == 0;
    rhs = child;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    assert index == 0;
    return rhs;
  }

  @Override
  public String toString() {
    return "updateEnv(" + name + ", " + rhs + ")";
  }
}
