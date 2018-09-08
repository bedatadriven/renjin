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

import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.LocalVarAllocator;
import org.renjin.compiler.codegen.var.VariableMap;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

public class InlineEmitContext implements EmitContext {

  private final EmitContext parentContext;
  private final List<CompiledSexp> parameters;
  private final VariableMap variableMap;
  private final VariableStrategy returnVariable;
  private final LabelMap labelMap = new LabelMap();
  private final Label exitLabel;

  public InlineEmitContext(EmitContext parentContext,
                           List<CompiledSexp> parameters,
                           VariableMap variableMap,
                           VariableStrategy returnVariable) {
    this.parentContext = parentContext;
    this.parameters = parameters;
    this.variableMap = variableMap;
    this.returnVariable = returnVariable;
    this.exitLabel = new Label();
  }

  /**
   * @param parameterIndex the index of the parameter supplied to the function (NOT the index of the closure's formal!)
   * @return the {@link CompiledSexp} for the given parameter
   */
  @Override
  public CompiledSexp getParamExpr(int parameterIndex) {
    return parameters.get(parameterIndex);
  }

  @Override
  public VariableStrategy getVariable(LValue lhs) {
    return variableMap.getStorage(lhs);
  }

  @Override
  public int getContextVarIndex() {
    return parentContext.getContextVarIndex();
  }

  @Override
  public int getEnvironmentVarIndex() {
    return parentContext.getEnvironmentVarIndex();
  }

  @Override
  public LocalVarAllocator getLocalVarAllocator() {
    return parentContext.getLocalVarAllocator();
  }

  @Override
  public Label getBytecodeLabel(IRLabel label) {
    return labelMap.getBytecodeLabel(label);
  }

  @Override
  public void writeReturn(InstructionAdapter mv, CompiledSexp returnExpr) {
    returnVariable.store(this, mv, returnExpr);
    mv.goTo(exitLabel);
  }

  @Override
  public void writeDone(InstructionAdapter mv) {
    mv.mark(exitLabel);
  }
}
