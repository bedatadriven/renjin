/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.eval.Support;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.*;

import java.util.List;
import java.util.Map;

public class DynamicCall implements Expression {

  private final FunctionCall call;
  private final String functionName;
  private final List<IRArgument> arguments;

  public DynamicCall(FunctionCall call, String functionName, List<IRArgument> arguments) {
    this.call = call;
    this.functionName = functionName;
    this.arguments = arguments;
  }

  public String getFunctionName() {
    return functionName;
  }

  @Override
  public boolean isPure() {
    return false;
  }

  public boolean isArgumentNamed(int i) {
    return arguments.get(i).isNamed();
  }

  public IRArgument getArgument(int index) {
    return arguments.get(index);
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
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ValueBounds getValueBounds() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        writeCall(context, mv);
      }
    };
  }

  private void writeCall(EmitContext context, InstructionAdapter mv) {

    // First find function
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.aconst(functionName);
    mv.invokevirtual(Type.getInternalName(LocalEnvironment.class), "findFunctionOrThrow",
          Type.getMethodDescriptor(Type.getType(Function.class),
              Type.getType(Context.class),
              Type.getType(String.class)), false);

    // Now we need to invoke:
    // SEXP Function::apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] arguments)
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
    context.constantSexp(call).loadSexp(context, mv);
    mv.checkcast(Type.getType(FunctionCall.class));
    loadArgumentNames(context, mv);
    loadArgumentValues(context, mv);

    mv.invokeinterface(Type.getInternalName(Function.class), "apply",
        Type.getMethodDescriptor(Type.getType(SEXP.class),
            Type.getType(Context.class),
            Type.getType(Environment.class),
            Type.getType(FunctionCall.class),
            Type.getType(String[].class),
            Type.getType(SEXP[].class)));

  }

  private void loadArgumentValues(EmitContext context, InstructionAdapter mv) {
    mv.iconst(arguments.size());
    mv.newarray(Type.getType(SEXP.class));
    for (int i = 0; i < arguments.size(); i++) {
      mv.dup();
      mv.iconst(i);
      arguments.get(i).getExpression().getCompiledExpr(context).loadSexp(context, mv);
      mv.visitInsn(Opcodes.AASTORE);
    }
  }

  private void loadArgumentNames(EmitContext context, InstructionAdapter mv) {
    if(!anyNamedArguments()) {
      loadEmptyNamesArray(context, mv);

    } else {
      // Maybe maintain a pool of argument names?
      mv.iconst(arguments.size());
      mv.newarray(Type.getType(String.class));
      for (int i = 0; i < arguments.size(); i++) {
        IRArgument argument = arguments.get(i);
        if(argument.isNamed()) {
          mv.dup();
          mv.iconst(i);
          mv.aconst(argument.getName());
          mv.visitInsn(Opcodes.AASTORE);
        }
      }
    }
  }

  private void loadEmptyNamesArray(EmitContext context, InstructionAdapter mv) {
    mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Support.class), "UNNAMED_ARGUMENTS_" + arguments.size(),
        Type.getDescriptor(String[].class));
  }

  private boolean anyNamedArguments() {
    for (IRArgument argument : arguments) {
      if(argument.isNamed()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "dynamic " + functionName + "(" + Joiner.on(", ").join(arguments) + ")";
  }

}
