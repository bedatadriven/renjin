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

import org.renjin.compiler.builtins.*;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.eval.Context;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Call to a builtin function
 */
public class BuiltinCall implements CallExpression {

  private final RuntimeState runtimeState;
  private FunctionCall call;
  private String primitiveName;
  private final List<IRArgument> arguments;

  private final Specializer specializer;
  
  private Specialization specialization = UnspecializedCall.INSTANCE;

  public BuiltinCall(RuntimeState runtimeState, FunctionCall call, String primitiveName, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.primitiveName = primitiveName;
    this.call = call;
    this.arguments = Lists.newArrayList(arguments);
    this.specializer = BuiltinSpecializers.INSTANCE.get(primitiveName);
  }

  public BuiltinCall(RuntimeState runtimeState, String primitiveName, Specializer specializer, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.primitiveName = primitiveName;
    this.arguments = Lists.newArrayList(arguments);
    this.specializer = specializer;
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
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    List<ArgumentBounds> argumentTypes = new ArrayList<>();
    for (IRArgument argument : arguments) {
      argumentTypes.add(new ArgumentBounds(
          argument.getName(),
          argument.getExpression(),
          argument.getExpression().updateTypeBounds(typeMap)));
    }
    specialization = specializer.trySpecialize(runtimeState, argumentTypes);
    
    return specialization.getResultBounds();
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
    try {
      specialization.emitAssignment(emitContext, mv, statement, arguments);
    } catch (FailedToSpecializeException e) {
      emitUnspecializedAssigment(emitContext, mv, statement);
//      throw new NotCompilableException(call, "Failed to specialize .Primitive(" + primitiveName + ")");
    }
  }

  private void emitUnspecializedAssigment(EmitContext emitContext, InstructionAdapter mv, Assignment statement) {
    VariableStrategy lhs = emitContext.getVariable(statement.getLHS());
    SexpExpr expr = new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        writeBuiltinCall(context, mv);
      }
    };

    lhs.store(emitContext, mv, expr);
  }

  @Override
  public void emitExecute(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException("TODO");
  }

  private void writeBuiltinCall(EmitContext context, InstructionAdapter mv) {

    // Invoke the doApply method of the generated class
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

    for (int i = 0; i < arguments.size(); i++) {
      arguments.get(i).getExpression().getCompiledExpr(context);
    }

    String generatedClass = WrapperGenerator2.toFullJavaName(primitiveName)
        .replace('.', '/');

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, generatedClass, "doApply", applyDescriptor(), false);

  }

  private String applyDescriptor() {
    Type[] argumentTypes = new Type[arguments.size() + 2];
    int argIndex = 0;
    argumentTypes[argIndex++] = Type.getType(Context.class);
    argumentTypes[argIndex++] = Type.getType(Environment.class);
    for (int i = 0; i < arguments.size(); i++) {
      argumentTypes[argIndex++] = Type.getType(SEXP.class);
    }

    return Type.getMethodDescriptor(Type.getType(SEXP.class), argumentTypes);
  }


  @Override
  public String toString() {
    return "(" + primitiveName + " " + Joiner.on(" ").join(arguments) + ")";
  }


}
