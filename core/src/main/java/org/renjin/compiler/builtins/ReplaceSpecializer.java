/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.compiler.builtins.subset.UpdateElementCall;
import org.renjin.compiler.ir.ArgumentBounds;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Specializes {@code [<- } calls
 */
public class ReplaceSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> argumentTypes) {
    List<ValueBounds> listValueBounds = new ArrayList<>();
    Iterator<ArgumentBounds> it = argumentTypes.iterator();
    while (it.hasNext()) {
      listValueBounds.add(it.next().getValueBounds());
    }
    if(argumentTypes.size() == 3) {
      
      
      ValueBounds inputVector = listValueBounds.get(0);
      ValueBounds subscript = listValueBounds.get(1);
      ValueBounds replacement = listValueBounds.get(2);
      
      if(subscript.getLength() == 1 && replacement.getLength() == 1 &&
          inputVector.getTypeSet() == replacement.getTypeSet()) {
        return new UpdateElementCall(inputVector, subscript, replacement);
      }
      
    }

    return UnspecializedCall.INSTANCE;
  }
}
