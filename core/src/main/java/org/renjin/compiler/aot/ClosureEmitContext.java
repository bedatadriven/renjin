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
package org.renjin.compiler.aot;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.LocalVarAllocator;
import org.renjin.compiler.codegen.var.SexpLocalVar;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.HashMap;
import java.util.Map;

public class ClosureEmitContext implements EmitContext {

  public static final int CONTEXT_VAR_INDEX = 0;
  public static final int ENVIRONMENT_VAR_INDEX = 1;

  private final Map<IRLabel, Label> labelMap = new HashMap<>();
  private final Map<LValue, VariableStrategy> variableMap = new HashMap<>();

  private final LocalVarAllocator varAllocator;
  private final ClassBuffer classBuffer;

  private int nextFrameVar = 0;

  public ClosureEmitContext(ClassBuffer classBuffer, PairList formals) {
    this.classBuffer = classBuffer;
    varAllocator = new LocalVarAllocator(ENVIRONMENT_VAR_INDEX + 1);
    for (PairList.Node node : formals.nodes()) {
      variableMap.put(new EnvironmentVariable(node.getTag()), new FrameVariableStrategy(node.getTag(), nextFrameVar++));
    }
  }

  public ListVector getFrameVariableNames() {
    SEXP[] names = new SEXP[nextFrameVar];

    for (VariableStrategy variable : variableMap.values()) {
      if(variable instanceof FrameVariableStrategy) {
        FrameVariableStrategy frameVar = (FrameVariableStrategy) variable;
        names[frameVar.getFrameIndex()] = frameVar.getName();
      }
    }
    return new ListVector(names);
  }

  public void loadSymbolPromise(Symbol symbol, InstructionAdapter mv) {
    for (VariableStrategy value : variableMap.values()) {
      if(value instanceof FrameVariableStrategy) {
        FrameVariableStrategy frameVar = (FrameVariableStrategy) value;
        if(frameVar.getName() == symbol) {
          loadFrameVarPromise(frameVar, mv);
        }
      }
    }
  }

  private void loadFrameVarPromise(FrameVariableStrategy frameVar, InstructionAdapter mv) {
    mv.visitVarInsn(Opcodes.ALOAD, ENVIRONMENT_VAR_INDEX);
    mv.iconst(frameVar.getFrameIndex());
    mv.invokevirtual(Type.getInternalName(FunctionEnvironment.class), "promise",
        Type.getMethodDescriptor(Type.getType(SEXP.class), Type.INT_TYPE), false);
  }

  @Override
  public int getContextVarIndex() {
    return CONTEXT_VAR_INDEX;
  }

  @Override
  public int getEnvironmentVarIndex() {
    return ENVIRONMENT_VAR_INDEX;
  }

  @Override
  public LocalVarAllocator getLocalVarAllocator() {
    return varAllocator;
  }

  @Override
  public Label getBytecodeLabel(IRLabel label) {
    return labelMap.computeIfAbsent(label, l -> new Label());
  }

  @Override
  public void writeReturn(InstructionAdapter mv, CompiledSexp returnExpr) {
    returnExpr.loadSexp(this, mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void writeDone(InstructionAdapter mv) {
  }

  @Override
  public CompiledSexp getParamExpr(int parameterIndex) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VariableStrategy getVariable(LValue lhs) {
    return variableMap.computeIfAbsent(lhs, var -> {
      if(var instanceof EnvironmentVariable) {
        return new FrameVariableStrategy(((EnvironmentVariable) var).getName(), nextFrameVar++);
      } else {
        return new SexpLocalVar(lhs, null, getLocalVarAllocator().reserveObject());
      }
    });
  }

  @Override
  public CompiledSexp constantSexp(SEXP sexp) {
    return classBuffer.sexp(sexp);
  }
}
