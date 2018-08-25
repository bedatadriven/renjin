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
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
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
    ValueBounds inputVector = arguments.get(0).getBounds();
    ValueBounds replacement = arguments.get(numArguments - 1).getBounds();

    ValueBounds[] subscripts = new ValueBounds[numArguments - 2];
    for (int i = 1; i < numArguments - 1; i++) {
      subscripts[i - 1] = arguments.get(i).getBounds();
    }

    if(subscripts.length > 1) {
      // Matrix replacements are a bit easier because it never changes the shape
      // of the input source.
      return new MatrixReplacement(inputVector, subscripts, replacement);

    }

    if(subscripts.length == 1 &&
       subscripts[0].getLength() == 1 &&
       replacement.getLength() == 1 &&
       inputVector.getTypeSet() == replacement.getTypeSet()) {

      return new UpdateElementCall(inputVector, subscripts[0], replacement);
    }

    return UnspecializedCall.INSTANCE;
  }

}
