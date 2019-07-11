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
import org.renjin.compiler.codegen.FunctionLoader;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.SexpLoader;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
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
        writeCall(emitContext, mv);
      }
    };
  }

  @Override
  public void emitExecute(EmitContext emitContext, InstructionAdapter mv) {
    writeCall(emitContext, mv);
    mv.pop();
  }

  private void writeCall(EmitContext emitContext, InstructionAdapter mv) {
    SexpLoader callLoader = (c, m) -> c.constantSexp(call).loadSexp(c, m);

    writeCall(emitContext, mv,
        functionLoader,
        callLoader,
        argumentNames(call),
        argumentPromises(call),
        forwardedArgumentIndex);
  }

  static void writeCall(EmitContext context,
                        InstructionAdapter mv,
                        FunctionLoader functionLoader,
                        SexpLoader call,
                        List<String> names,
                        List<SexpLoader> argumentPromises,
                        int forwardedArgumentIndex) {

    // First find function
    functionLoader.loadFunction(context, mv);

    // Now we need to invoke:
    //   SEXP applyPromised(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch);
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

    // The original call object
    call.loadSexp(context, mv);
    mv.checkcast(Type.getType(FunctionCall.class));

    // Argument Names
    loadArgumentNames(mv, names);
    loadPromises(context, mv, argumentPromises);

    if(forwardedArgumentIndex == -1) {
      // Can pass directly to applyPromised

      // No Dispatch table
      mv.aconst(null);

      mv.invokeinterface(Type.getInternalName(Function.class), "applyPromised",
          Type.getMethodDescriptor(Type.getType(SEXP.class),
              Type.getType(Context.class),
              Type.getType(Environment.class),
              Type.getType(FunctionCall.class),
              Type.getType(String[].class),
              Type.getType(SEXP[].class),
              Type.getType(DispatchTable.class)));

    } else {

      // Otherwise need the forwarded arguments inserted first

      mv.invokeinterface(Type.getInternalName(Function.class), "expandThenApplyPromised",
          Type.getMethodDescriptor(Type.getType(SEXP.class),
              Type.getType(Context.class),
              Type.getType(Environment.class),
              Type.getType(FunctionCall.class),
              Type.getType(String[].class),
              Type.getType(SEXP[].class)));

    }
  }


  static void loadArgumentNames(InstructionAdapter mv, List<String> names) {
    mv.iconst(names.size());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(String.class));

    for (int i = 0; i < names.size(); i++) {
      if(names.get(i) != null) {
        mv.visitInsn(Opcodes.DUP);
        mv.iconst(i);
        mv.aconst(names.get(i));
        mv.visitInsn(Opcodes.AASTORE);
      }
    }
  }

  static void loadPromises(EmitContext context, InstructionAdapter mv, List<SexpLoader> argumentPromises) {
    mv.iconst(argumentPromises.size());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(SEXP.class));

    for (int i = 0; i < argumentPromises.size(); i++) {
      if(argumentPromises.get(i) != null) {
        mv.visitInsn(Opcodes.DUP);
        mv.iconst(i);
        argumentPromises.get(i).loadSexp(context, mv);
        mv.visitInsn(Opcodes.AASTORE);
      }
    }
  }

  private static boolean anyNamed(List<String> names) {
    for (String name : names) {
      if(name != null) {
        return true;
      }
    }
    return false;
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
