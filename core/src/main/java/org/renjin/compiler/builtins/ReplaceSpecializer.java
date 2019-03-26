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
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.List;

/**
 * Specializes {@code [<- } calls
 */
public class ReplaceSpecializer implements Specializer, BuiltinSpecializer {


  @Override
  public String getName() {
    return "[<-";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    int numArguments = arguments.size();
    ArgumentBounds inputVector = arguments.get(0);
    ArgumentBounds replacement = arguments.get(numArguments - 1);

    ArgumentBounds[] subscripts = new ArgumentBounds[numArguments - 2];
    for (int i = 1; i < numArguments - 1; i++) {
      subscripts[i - 1] = arguments.get(i);
    }

    UpdateElementCall updateElementCall = UpdateElementCall.trySpecialize(inputVector, subscripts, replacement);
    if(updateElementCall != null) {
      return updateElementCall;
    }

    return new ReplaceSpecialization(inputVector, subscripts, replacement);
  }

}
