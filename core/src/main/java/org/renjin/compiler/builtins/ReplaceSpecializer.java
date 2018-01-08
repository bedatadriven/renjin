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
    if(arguments.size() == 3) {
      
      
      ValueBounds inputVector = arguments.get(0).getBounds();
      ValueBounds subscript = arguments.get(1).getBounds();
      ValueBounds replacement = arguments.get(2).getBounds();
      
      if(subscript.getLength() == 1 && replacement.getLength() == 1 &&
          inputVector.getTypeSet() == replacement.getTypeSet()) {
        return new UpdateElementCall(inputVector, subscript, replacement);
      }
      
    }

    return UnspecializedCall.INSTANCE;
  }

}
