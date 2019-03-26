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

import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.builtins.S3Specialization;
import org.renjin.compiler.builtins.Specialization;
import org.renjin.compiler.builtins.UnspecializedCall;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.FunctionCall;

import java.util.List;
import java.util.Map;

/**
 * Call to UseMethod
 */
public class UseMethodCall implements Expression {
  
  private RuntimeState runtimeState;
  private FunctionCall call;
  /**
   * The name of the generic method. 
   */
  private final String generic;
  
  private final List<IRArgument> arguments;

  private Specialization specialization = UnspecializedCall.INSTANCE;
  
  public UseMethodCall(RuntimeState runtimeState, FunctionCall call, String generic, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.call = call;
    this.generic = generic;
    this.arguments = arguments;
  }

  @Override
  public boolean isPure() {
    return specialization.isPure();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    ValueBounds objectBounds = typeMap.get(getObjectExpr());
    
    // Maybe see if we can avoid re-specializing entirely?
    this.specialization = S3Specialization.trySpecialize(generic, runtimeState, objectBounds,
        ArgumentBounds.create(arguments, typeMap));

    return specialization.getResultBounds();
  }

  private Expression getObjectExpr() {
    return arguments.get(0).getExpression();
  }

  @Override
  public ValueBounds getValueBounds() {
    return specialization.getResultBounds();
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return specialization.getCompiledExpr(emitContext, arguments);
  }

  @Override
  public void emitAssignment(EmitContext emitContext, InstructionAdapter mv, Assignment statement) {
    specialization.emitAssignment(emitContext, mv, statement, arguments);
  }

  @Override
  public void emitExecute(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex,
        arguments.get(childIndex).withExpression(child));
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    return arguments.get(index).getExpression();
  }

  @Override
  public String toString() {
    return "UseMethod(" + generic + ", " + Joiner.on(", ").join(arguments) + ")";
  }
}
