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
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

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

    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
    mv.visitLdcInsn(functionName);

    if(arguments.size() == 1 && !arguments.get(0).isNamed()) {
      writeDynamicCall1(context, mv);

    } else if(arguments.size() <= 5) {
      writeDynamicCallShort(context, mv);

    } else {
      throw new UnsupportedOperationException("TODO: "+  this);
    }
  }

  private void writeDynamicCall1(EmitContext context, InstructionAdapter mv) {

    childAt(0).getCompiledExpr(context).loadSexp(context, mv);

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Support.class), "invoke1",
        Type.getMethodDescriptor(
            Type.getType(SEXP.class),
            Type.getType(Context.class),
            Type.getType(Environment.class),
            Type.getType(SEXP.class)),
        false);
  }


  private void writeDynamicCallShort(EmitContext context, InstructionAdapter mv) {

    Type[] argumentTypes = new Type[getChildCount() * 2 + 3];
    int argIndex = 0;
    argumentTypes[argIndex++] = Type.getType(Context.class);
    argumentTypes[argIndex++] = Type.getType(Environment.class);
    argumentTypes[argIndex++] = Type.getType(String.class);
    for (int i = 0; i < getChildCount(); i++) {
      argumentTypes[argIndex++] = Type.getType(String.class);
      argumentTypes[argIndex++] = Type.getType(SEXP.class);
    }

    String descriptor = Type.getMethodDescriptor(Type.getType(SEXP.class), argumentTypes);

    mv.visitLdcInsn(functionName);

    for (int i = 0; i < getChildCount(); i++) {
      if(arguments.get(i).isNamed()) {
        mv.visitLdcInsn(arguments.get(i).getName());
      } else {
        mv.visitInsn(Opcodes.ACONST_NULL);
      }
      arguments.get(i).getExpression().getCompiledExpr(context).loadSexp(context, mv);
    }

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Support.class),
        "invoke" + arguments.size(),
        descriptor,
        false);
  }

  @Override
  public String toString() {
    return "dynamic " + functionName + "(" + Joiner.on(", ").join(arguments) + ")";
  }

}
