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
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

public class ApplyCallEmitContext implements EmitContext {
  @Override
  public int getContextVarIndex() {
    return 0;
  }

  @Override
  public int getEnvironmentVarIndex() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public LocalVarAllocator getLocalVarAllocator() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Label getBytecodeLabel(IRLabel label) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void writeReturn(InstructionAdapter mv, CompiledSexp returnExpr) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void writeDone(InstructionAdapter mv) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public CompiledSexp getParamExpr(int parameterIndex) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VariableStrategy getVariable(LValue lhs) {
    throw new UnsupportedOperationException("TODO");
  }
}
