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
package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.ExtraArgument;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.EllipsesVar;
import org.renjin.sexp.Symbol;

import java.util.ArrayList;
import java.util.List;

public class LoopBodyContext implements TranslationContext {

  private RuntimeState runtimeState;
  private List<ExtraArgument> ellipses;

  public LoopBodyContext(RuntimeState runtimeState) {
    this.runtimeState = runtimeState;
  }

  public boolean isEllipsesInitializationNeeded() {
    return ellipses != null;
  }

  @Override
  public List<IRArgument> getEllipsesArguments() {
    if(ellipses == null) {
      ellipses = runtimeState.findEllipses();
    }
    List<IRArgument> arguments = new ArrayList<>();
    for (int i = 0; i < ellipses.size(); i++) {
      arguments.add(new IRArgument(ellipses.get(i).getName(), new EllipsesVar(i)));
    }
    return arguments;
  }

  @Override
  public boolean isMissing(Symbol name) {
    return runtimeState.isMissing(name);
  }
}
