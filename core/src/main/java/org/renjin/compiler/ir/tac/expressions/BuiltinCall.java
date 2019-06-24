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

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.builtins.*;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.FunctionCall;

import java.util.ArrayList;
import java.util.List;

/**
 * Call to a builtin function
 */
public class BuiltinCall implements CallExpression {

  private final RuntimeState runtimeState;
  private FunctionCall call;
  private String primitiveName;
  private final List<IRArgument> arguments;

  /**
   * The index with in the argument list in which forwarded arguments (...)
   * are to be inserted at runtime, or -1 if there are no forward arguments.
   */
  private int forwardedArgumentIndex = -1;

  private final Specializer specializer;
  
  private Specialization specialization = UnspecializedCall.INSTANCE;

  public BuiltinCall(RuntimeState runtimeState, FunctionCall call, String primitiveName, List<IRArgument> arguments, int forwardArgumentIndex) {
    this.runtimeState = runtimeState;
    this.primitiveName = primitiveName;
    this.call = call;
    this.arguments = Lists.newArrayList(arguments);
    this.specializer = BuiltinSpecializers.INSTANCE.get(primitiveName);
    this.forwardedArgumentIndex = forwardArgumentIndex;
  }

  public BuiltinCall(RuntimeState runtimeState, String primitiveName, Specializer specializer, List<IRArgument> arguments, int forwardedArgumentIndex) {
    this.runtimeState = runtimeState;
    this.primitiveName = primitiveName;
    this.arguments = Lists.newArrayList(arguments);
    this.specializer = specializer;
    this.forwardedArgumentIndex = forwardedArgumentIndex;
  }

  public String getPrimitiveName() {
    return primitiveName;
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
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex,
        arguments.get(childIndex).withExpression(child));
  }
  
  @Override
  public boolean isPure() {
    return specialization.isPure();
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    List<ArgumentBounds> argumentTypes = new ArrayList<>();
    for (IRArgument argument : arguments) {
      argumentTypes.add(new ArgumentBounds(
          argument.getName(),
          argument.getExpression(),
          argument.getExpression().updateTypeBounds(typeMap)));
    }

    // If call involves forwarded arguments, then specialization at
    // compile time becomes more difficult

    if(forwardedArgumentIndex != -1) {
      specialization = specializer.trySpecialize(runtimeState, argumentTypes, forwardedArgumentIndex);

    } else {
      specialization = specializer.trySpecialize(runtimeState, argumentTypes);
    }

    return specialization.getResultBounds();
  }

  @Override
  public ValueBounds getValueBounds() {
    return specialization.getResultBounds();
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    try {
      return specialization.getCompiledExpr(emitContext, call, arguments);
    } catch (FailedToSpecializeException e) {
      throw new NotCompilableException(call, "Failed to specialize .Primitive(" + primitiveName + ")");
    }
  }

  @Override
  public void emitAssignment(EmitContext emitContext, InstructionAdapter mv, Assignment statement) {
    try {
      specialization.emitAssignment(emitContext, mv, statement, call, arguments);
    } catch (FailedToSpecializeException e) {
      throw new NotCompilableException(call, "Failed to specialize .Primitive(" + primitiveName + ")");
    }
  }

  @Override
  public void emitExecute(EmitContext emitContext, InstructionAdapter mv) {
    specialization.getCompiledExpr(emitContext, call, arguments).loadSexp(emitContext, mv);
    mv.visitInsn(Opcodes.POP);
  }

  @Override
  public String toString() {
    return "(" + primitiveName + " " + Joiner.on(" ").join(arguments) + ")";
  }

}
