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

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.sexp.LogicalVector;

import java.util.List;

public class IsNumericSpecializer implements Specializer {

  private final JvmMethod fallback;

  public IsNumericSpecializer() {
    fallback = Iterables.getOnlyElement(JvmMethod.findOverloads(Types.class, "is.numeric", null));
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    if(arguments.size() == 1) {
      ArgumentBounds x = arguments.get(0);
      if(x.getTypeSet() == TypeSet.DOUBLE) {
        return new ConstantCall(LogicalVector.TRUE);
      } else if(x.getTypeSet() == TypeSet.INT || x.getTypeSet() == (TypeSet.INT | TypeSet.DOUBLE)) {
        // Only true if not a factor!
        if(!x.getBounds().isFlagSet(ValueBounds.MAYBE_CLASS)) {
          return new ConstantCall(LogicalVector.TRUE);
        }
      }
    }
    return new StaticMethodCall(fallback, arguments);
  }
}
