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
import org.renjin.compiler.codegen.ArgListGen;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.FunctionLoader;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.SexpLoader;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.ArgList;
import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;

public class DynamicCall implements Expression {

  private final FunctionLoader functionLoader;
  private final FunctionCall call;
  private final String functionName;

  /**
   * The index of the ... argument, or -1 there is none.
   */
  private final int forwardedArgumentIndex;

  public DynamicCall(FunctionLoader functionLoader, FunctionCall call, String functionName) {
    this.functionLoader = functionLoader;
    this.call = call;
    this.functionName = functionName;
    this.forwardedArgumentIndex = call.findEllipsisArgumentIndex();

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
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    return ValueBounds.UNBOUNDED;
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

  @Override
  public void emitExecute(EmitContext emitContext, InstructionAdapter mv) {
    writeCall(emitContext, mv);
    mv.pop();
  }

  private void writeCall(EmitContext context, InstructionAdapter mv) {

    // First find function
    functionLoader.loadFunction(context, mv);

    // Now we need to invoke:
    //   SEXP applyPromised(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch);
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

    ArgListGen argListGen = new ArgListGen(context, mv)
        .names(argumentNames(call))
        .values(argumentPromises(call));

    if(forwardedArgumentIndex == -1) {
      argListGen.load();
    } else {
      // Environment is needed on the stack
      mv.dup();
      argListGen.expandLoad(forwardedArgumentIndex);
    }

    // The original call object
    context.constantSexp(call).loadSexp(context, mv);
    mv.checkcast(Type.getType(FunctionCall.class));

    // No Dispatch table
    mv.aconst(null);

    mv.invokeinterface(Type.getInternalName(Function.class), "applyPromised",
        Type.getMethodDescriptor(Type.getType(SEXP.class),
            Type.getType(Context.class),
            Type.getType(Environment.class),
            Type.getType(ArgList.class),
            Type.getType(FunctionCall.class),
            Type.getType(DispatchTable.class)));
  }

  static List<String> argumentNames(FunctionCall call) {
    List<String> names = new ArrayList<>();
    for (PairList.Node node : call.getArguments().nodes()) {
      if(node.getValue() != Symbols.ELLIPSES) {
        if(!node.hasTag()) {
          names.add(null);
        } else {
          names.add(node.getName());
        }
      }
    }
    return names;
  }

  static List<SexpLoader> argumentPromises(FunctionCall call) {
    List<SexpLoader> promises = new ArrayList<>();
    for (PairList.Node node : call.getArguments().nodes()) {
      if(node.getValue() != Symbols.ELLIPSES) {
        promises.add((context, mv) -> loadArgumentPromise(context, mv, node.getValue()));
      }
    }
    return promises;
  }


  static void loadArgumentPromise(EmitContext context, InstructionAdapter mv, SEXP argumentValue) {
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

  static void loadSymbolPromise(EmitContext context, InstructionAdapter mv, Symbol symbol) {
    ClosureEmitContext closureEmitContext = (ClosureEmitContext) context;
    closureEmitContext.loadSymbolPromise(symbol, mv);
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
