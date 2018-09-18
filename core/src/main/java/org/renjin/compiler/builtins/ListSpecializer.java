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

import org.renjin.compiler.ir.NamedShape;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.StringArrayVector;

import java.util.List;

public class ListSpecializer implements BuiltinSpecializer {
  @Override
  public String getName() {
    return "list";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    AtomicVector names = namesFromArguments(arguments);
    List<ValueBounds> valueBounds = ArgumentBounds.withoutNames(arguments);

    ValueBounds listBounds = ValueBounds.builder()
        .setTypeSet(TypeSet.LIST)
        .addFlags(ValueBounds.LENGTH_NON_ZERO, arguments.size() > 0)
        .addFlags(ValueBounds.LENGTH_ONE, arguments.size() == 1)
        .addFlags(ValueBounds.MAYBE_NAMES, names != Null.INSTANCE)
        .setShape(new NamedShape(names, valueBounds))
        .build();

    return new ListCall(arguments, names, listBounds);

  }

  private AtomicVector namesFromArguments(List<ArgumentBounds> arguments) {
    String[] names = new String[arguments.size()];
    boolean hasNames = false;
    for (int i = 0; i < arguments.size(); i++) {
      ArgumentBounds argument = arguments.get(i);
      if(argument.isNamed()) {
        names[i] = argument.getName();
        hasNames = true;
      }
    }
    if(hasNames) {
      return new StringArrayVector(names);
    } else {
      return Null.INSTANCE;
    }
  }
}
