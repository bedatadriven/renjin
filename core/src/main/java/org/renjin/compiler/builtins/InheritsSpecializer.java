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
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Attributes;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.List;

public class InheritsSpecializer implements BuiltinSpecializer {

  private final JvmMethod fallback;

  public InheritsSpecializer() {
    fallback = Iterables.getOnlyElement(JvmMethod.findOverloads(Attributes.class, "inherits", "inherits"));
  }

  @Override
  public String getName() {
    return "inherits";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    InvalidSyntaxException.checkInternalArity(getName(), 3, arguments.size());
    ArgumentBounds x = arguments.get(0);
    ArgumentBounds what = arguments.get(1);
    ArgumentBounds which = arguments.get(2);

    // The 'which' flag determines whether we return an integer array (TRUE) or a boolean flag (FALSE)
    if(which.getBounds().isConstantFlagEqualTo(true)) {

    }


    return new StaticMethodCall(fallback, ValueBounds.builder()
      .setTypeSet(TypeSet.INT | TypeSet.LOGICAL)
      .build());
  }
}
