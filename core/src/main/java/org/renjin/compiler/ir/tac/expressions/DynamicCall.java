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
import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.builtins.BuiltinSpecializers;
import org.renjin.compiler.builtins.Specialization;
import org.renjin.compiler.builtins.Specializer;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.SexpLoader;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;

public class DynamicCall implements Expression {

  private final FunctionCall call;
  private final RuntimeState runtimeState;
  private Expression functionExpr;

  private final List<IRArgument> arguments;

  /**
   * The index of the ... argument, or -1 there is none.
   */
  private final int forwardedArgumentIndex;

  private ValueBounds bounds = ValueBounds.UNBOUNDED;
  private Specialization specialization;

  public DynamicCall(RuntimeState runtimeState, FunctionCall call, Expression functionExpr, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.functionExpr = functionExpr;
    this.call = call;
    this.forwardedArgumentIndex = call.findEllipsisArgumentIndex();

    this.arguments = arguments;
  }

  @Override
  public boolean isPure() {
    if(bounds.isConstant()) {
      return true;
    }
    return false;
  }

  public Expression getFunctionExpr() {
    return functionExpr;
  }

  public void setFunctionExpr(Expression functionExpr) {
    this.functionExpr = functionExpr;
  }

  public IRArgument getArgument(int index) {
    throw new IllegalArgumentException();
  }


  @Override
  public int getChildCount() {
    return arguments.size() + 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return functionExpr;
    } else {
      return arguments.get(index - 1).getExpression();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      functionExpr = child;
    } else {
      arguments.set(childIndex - 1, arguments.get(childIndex - 1).withExpression(child));
    }
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    ValueBounds functionBounds = functionExpr.updateTypeBounds(typeMap);
    List<ArgumentBounds> argumentBounds = ArgumentBounds.create(arguments, typeMap);

    if(functionBounds.isConstant() && functionBounds.getConstantValue() instanceof BuiltinFunction) {
      BuiltinFunction builtin = (BuiltinFunction) functionBounds.getConstantValue();
      Specializer specializer = BuiltinSpecializers.INSTANCE.get(builtin.getName());
      specialization = specializer.trySpecialize(runtimeState, argumentBounds);
      bounds = specialization.getResultBounds();

    } else {
      bounds = ValueBounds.UNBOUNDED;
    }

    return bounds;
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

  private void writeCall(EmitContext context, InstructionAdapter mv) {

    // First find function
    functionExpr.getCompiledExpr(context).loadSexp(context, mv, Type.getType(Function.class));

    // Now we need to invoke:
    //   SEXP applyPromised(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] promisedArguments, DispatchTable dispatch);
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

    // The original call object
    context.constantSexp(call).loadSexp(context, mv);
    mv.checkcast(Type.getType(FunctionCall.class));

    // Argument Names
    loadArgumentNames(mv, argumentNames(call));

    // Need to push arguments on the stack
    loadPromises(context, mv, argumentPromises(call));

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

  List<SexpLoader> argumentPromises(FunctionCall call) {
    List<SexpLoader> promises = new ArrayList<>();
    for (PairList.Node node : call.getArguments().nodes()) {
      if(node.getValue() != Symbols.ELLIPSES) {
        promises.add((context, mv) -> loadArgumentPromise(context, mv, node.getValue()));
      }
    }
    return promises;
  }

  void loadArgumentPromise(EmitContext context, InstructionAdapter mv, SEXP argument) {
    int argumentIndex = 0;
    if(argument == Symbol.MISSING_ARG) {
      mv.getstatic(Type.getInternalName(Symbol.class), "MISSING_ARG", Type.getDescriptor(Symbol.class));

    } else if(argument instanceof Symbol) {
      loadSymbolPromise(context, mv, (Symbol) argument);
      argumentIndex++;

    } else {
      IRArgument irArgument = arguments.get(argumentIndex);
      Expression expression = irArgument.getExpression();
      if(expression.getValueBounds().getTypeSet() == TypeSet.PROMISE) {
        expression.getCompiledExpr(context).loadSexp(context, mv);
      } else {
        // We were able to conclude that this argument could be eagerly evaluated and
        // did so. Now we have to repromise with the original expression.
        context.constantSexp(argument).loadSexp(context, mv);
        expression.getCompiledExpr(context).loadSexp(context, mv);
        mv.invokeinterface(Type.getInternalName(SEXP.class), "repromise",
            Type.getMethodDescriptor(Type.getType(SEXP.class), Type.getType(SEXP.class)));
      }
      argumentIndex++;
    }
  }

  static void loadSymbolPromise(EmitContext context, InstructionAdapter mv, Symbol symbol) {
    ClosureEmitContext closureEmitContext = (ClosureEmitContext) context;
    closureEmitContext.loadSymbolPromise(symbol, mv);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("call ").append(functionExpr).append("(");
    boolean needsComma = false;
    int argumentIndex = 0;

    for (PairList.Node node : call.getArguments().nodes()) {
      if(needsComma) {
        s.append(", ");
      }
      if(node.hasTag()) {
        s.append(node.getName()).append(" = ");
      }
      if(node.getValue() != Symbol.MISSING_ARG) {
        if(node.getValue() != Symbols.ELLIPSES) {
          IRArgument argument = arguments.get(argumentIndex);
          s.append(argument.getExpression());
          argumentIndex ++;
        } else {
          s.append("...");
        }
      }
      needsComma = true;
    }
    s.append(")");
    return s.toString();
  }

}
