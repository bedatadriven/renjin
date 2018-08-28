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

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.eval.MatchedArgumentPositions;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.Closure;
import org.renjin.sexp.FunctionCall;

import java.util.List;
import java.util.Map;


public class ClosureCall implements Expression {

  private final RuntimeState runtimeState;
  private final FunctionCall call;
  private final List<IRArgument> arguments;
  private final Closure closure;

  private final String debugName;

  private MatchedArgumentPositions matching;
  private InlinedFunction inlinedFunction;
  
  private ValueBounds returnBounds;

  public ClosureCall(RuntimeState runtimeState, FunctionCall call, Closure closure, String closureDebugName, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.call = call;
    this.closure = closure;
    this.arguments = arguments;
    this.debugName = closureDebugName;

    this.matching = MatchedArgumentPositions.matchIRArguments(closure, arguments);
    this.returnBounds = ValueBounds.UNBOUNDED;
  }


  @Override
  public boolean isPure() {
    if(inlinedFunction == null) {
      return false;
    }
    return inlinedFunction.isPure();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    if(inlinedFunction == null) {
      try {
        this.inlinedFunction = new InlinedFunction(runtimeState, closure, this.matching.getSuppliedFormals());
      } catch (NotCompilableException e) {
        throw new NotCompilableException(call, e);
      }
    }
    
    if(matching.hasExtraArguments()) {
      throw new NotCompilableException(call, "Extra arguments not supported");
    }
   
    returnBounds = inlinedFunction.updateBounds(ArgumentBounds.create(arguments, typeMap));

    return returnBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return returnBounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    throw new UnsupportedOperationException("TODO");
  }


  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.get(childIndex).setExpression(child);
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
  public void emitAssignment(EmitContext emitContext, InstructionAdapter mv, Assignment statement) {

    if(matching.hasExtraArguments()) {
      throw new NotCompilableException(call, "Extra arguments not supported");
    }

    VariableStrategy lhs = emitContext.getVariable(statement.getLHS());

    inlinedFunction.emitInline(emitContext, mv, matching, arguments, lhs);
  }

  @Override
  public void emitExecute(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public String toString() {
    return debugName + "(" + Joiner.on(", ").join(arguments) + ")";
  }

}
