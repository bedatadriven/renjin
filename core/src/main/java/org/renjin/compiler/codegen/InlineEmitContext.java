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
package org.renjin.compiler.codegen;

import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.Symbol;

import java.util.Map;

public class InlineEmitContext extends EmitContext {


  private Map<Symbol, InlineParamExpr> inlinedParameters = Maps.newHashMap();

  private final Label exitLabel;

  public InlineEmitContext(ControlFlowGraph cfg, int paramCount, VariableSlots childSlots) {
    super(cfg, paramCount, childSlots);
    this.exitLabel = new Label();
  }

  public InlineParamExpr getInlineParameter(Symbol param) {
    InlineParamExpr paramExpr = inlinedParameters.get(param);
    if(paramExpr == null) {
      throw new IllegalStateException("No expression set for parameter " + param);
    }
    return paramExpr;
  }


  public void setInlineParameter(Symbol parameterName, InlineParamExpr value) {
    inlinedParameters.put(parameterName, value);
  }

  @Override
  public void loadParam(InstructionAdapter mv, Symbol param) {
    InlineParamExpr value = inlinedParameters.get(param);
    value.load(mv);
  }

  @Override
  public void writeReturn(InstructionAdapter mv, Type returnType) {
    mv.goTo(exitLabel);
  }

  @Override
  public void writeDone(InstructionAdapter mv) {
    super.writeDone(mv);
    mv.mark(exitLabel);
  }
}
