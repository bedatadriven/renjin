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

package org.renjin.compiler.builtins;

import org.renjin.compiler.builtins.*;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.List;

/**
 * Specializes the "[[" operator.
 */
public class SingleSubsetSpecializer implements BuiltinSpecializer {

  @Override
  public String getName() {
    return "[[";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {


    // Case of x[[i]]:
    if(argumentTypes.size() == 2) {

      ValueBounds source = argumentTypes.get(0);
      ValueBounds index = argumentTypes.get(1);

      if(TypeSet.isDefinitelyNumeric(index) &&
          index.isLengthConstant() &&
          index.getLength() == 1) {

        if(source.getTypeSet() == TypeSet.LIST) {
          return new GetListElement(source, index);

        } else if(TypeSet.isDefinitelyAtomic(source.getTypeSet())) {
          return new GetAtomicElement(source, index);
        }

      }

      throw new UnsupportedOperationException("TODO");
    }

    return UnspecializedCall.INSTANCE;
  }
}
