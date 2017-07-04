/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.compiler.codegen;

import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Symbol;

import java.util.List;

public class ApplyMethodContext extends EmitContext {

  private Symbol argumentName;
  private Type argumentType;

  public ApplyMethodContext(ControlFlowGraph cfg, Symbol argumentName, Type argumentType, VariableSlots variableSlots) {
    super(cfg, argumentType.getSize(), variableSlots);
    this.argumentName = argumentName;
    this.argumentType = argumentType;
  }

  @Override
  public void loadParam(InstructionAdapter mv, Symbol param) {
    if(param.equals(argumentName)) {
      mv.load(0, argumentType);
    } else {
      throw new IllegalStateException("argument: " + param);
    }

  }
}
