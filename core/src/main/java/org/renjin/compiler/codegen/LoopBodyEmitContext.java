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
package org.renjin.compiler.codegen;

import org.renjin.compiler.CompiledLoopBody;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.LocalVarAllocator;
import org.renjin.compiler.codegen.var.VariableMap;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Symbol;

public class LoopBodyEmitContext implements EmitContext {
  private final LocalVarAllocator localVars;
  private final VariableMap variableMap;
  private final LabelMap labelMap = new LabelMap();

  public LoopBodyEmitContext(LocalVarAllocator localVars, VariableMap variableMap) {
    this.localVars = localVars;
    this.variableMap = variableMap;
  }

  @Override
  public int getContextVarIndex() {
    return CompiledLoopBody.CONTEXT_PARAM_INDEX;
  }

  @Override
  public int getEnvironmentVarIndex() {
    return CompiledLoopBody.ENV_PARAM_INDEX;
  }

  @Override
  public LocalVarAllocator getLocalVarAllocator() {
    return localVars;
  }

  @Override
  public Label getBytecodeLabel(IRLabel label) {
    return labelMap.getBytecodeLabel(label);
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
  public CompiledSexp getParamExpr(Symbol paramName) {
    throw new IllegalStateException("Loop bodies do not have parameters");
  }

  @Override
  public VariableStrategy getVariable(LValue lhs) {
    return variableMap.getStorage(lhs);
  }
}
