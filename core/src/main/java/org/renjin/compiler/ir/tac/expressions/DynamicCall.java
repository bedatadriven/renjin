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

import org.renjin.compiler.aot.ClosureEmitContext;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.eval.Support;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.Map;

public class DynamicCall implements Expression {

  private final FunctionCall call;
  private final String functionName;

  public DynamicCall(FunctionCall call, String functionName) {
    this.call = call;
    this.functionName = functionName;
  }

  public String getFunctionName() {
    return functionName;
  }

  @Override
  public boolean isPure() {
    return false;
  }

  public IRArgument getArgument(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
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
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
    mv.aconst(functionName);
    mv.invokevirtual(Type.getInternalName(Context.class), "evaluateFunction",
          Type.getMethodDescriptor(Type.getType(Function.class),
              Type.getType(Environment.class),
              Type.getType(String.class)), false);

    // Now we need to invoke:
    //   SEXP applyPromised(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch);
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

    context.constantSexp(call).loadSexp(context, mv);
    mv.checkcast(Type.getType(FunctionCall.class));

    loadArgumentNames(context, mv);
    loadArgumentValues(context, mv);

    mv.aconst(null);

    mv.invokeinterface(Type.getInternalName(Function.class), "applyPromised",
        Type.getMethodDescriptor(Type.getType(SEXP.class),
            Type.getType(Context.class),
            Type.getType(Environment.class),
            Type.getType(FunctionCall.class),
            Type.getType(String[].class),
            Type.getType(SEXP[].class),
            Type.getType(DispatchTable.class)));
  }

  private void loadArgumentValues(EmitContext context, InstructionAdapter mv) {

    int numArguments = call.getArguments().length();

    mv.iconst(numArguments);
    mv.newarray(Type.getType(SEXP.class));

    int i = 0;
    for (SEXP argumentValue : call.getArguments().values()) {
      mv.dup();
      mv.iconst(i);
      loadArgumentPromise(context, mv, argumentValue);
      mv.visitInsn(Opcodes.AASTORE);
      i++;
    }

  }

  private void loadArgumentPromise(EmitContext context, InstructionAdapter mv, SEXP argumentValue) {
    if(argumentValue instanceof Symbol) {
      loadSymbolPromise(context, mv, (Symbol)argumentValue);
    } else {
      context.constantSexp(argumentValue).loadSexp(context, mv);
      mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
      mv.invokeinterface(Type.getInternalName(SEXP.class), "promise", Type.getMethodDescriptor(
          Type.getType(SEXP.class),
          Type.getType(Environment.class)));
    }
  }

  private void loadSymbolPromise(EmitContext context, InstructionAdapter mv, Symbol symbol) {
    ClosureEmitContext closureEmitContext = (ClosureEmitContext) context;
    closureEmitContext.loadSymbolPromise(symbol, mv);
  }

  private void loadArgumentNames(EmitContext context, InstructionAdapter mv) {
    if(!anyNamedArguments()) {
      loadEmptyNamesArray(context, mv);

    } else {
      // Maybe maintain a pool of argument names?
      mv.iconst(call.getArguments().length());
      mv.newarray(Type.getType(String.class));
      int i = 0;
      for (PairList.Node node : call.getArguments().nodes()) {
        if(node.hasTag()) {
          mv.dup();
          mv.iconst(i);
          mv.aconst(node.getName());
          mv.visitInsn(Opcodes.AASTORE);
        }
        i++;
      }
    }
  }

  private void loadEmptyNamesArray(EmitContext context, InstructionAdapter mv) {
    mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Support.class), "UNNAMED_ARGUMENTS_" + call.getArguments().length(),
        Type.getDescriptor(String[].class));
  }

  private boolean anyNamedArguments() {
    for (PairList.Node node : call.getArguments().nodes()) {
      if(node.hasTag()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("dynamic ").append(functionName).append("(");
    boolean needsComma = false;
    for (PairList.Node node : call.getArguments().nodes()) {
      if(needsComma) {
        s.append(", ");
      }
      if(node.hasTag()) {
        s.append(node.getName()).append(" = ");
      }
      if(node.getValue() != Symbol.MISSING_ARG) {
        s.append(node.getValue());
      }
      needsComma = true;
    }
    s.append(")");
    return s.toString();
  }

}
