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

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.List;

public class SetAttributesSpecializer implements Specializer {
  private final PrimitiveModel primitive;

  public SetAttributesSpecializer() {
    primitive = new PrimitiveModel(Primitives.getBuiltinEntry("attributes<-"));
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    ArgumentBounds sexp = arguments.get(0);
    ArgumentBounds attributes = arguments.get(1);

    if(attributes.getTypeSet() == TypeSet.NULL) {
      return new IdentityCall(sexp.getExpression(), sexp.getBounds());

    } else {
      JvmMethod method = Iterables.getOnlyElement(primitive.getOverloads());
      ValueBounds resultBounds = ValueBounds.builder()
          .setTypeSet(sexp.getTypeSet())
          .addFlagsFrom(sexp.getBounds(),
              ValueBounds.FLAG_NO_NA |
              ValueBounds.FLAG_POSITIVE |
              ValueBounds.LENGTH_NON_ZERO |
              ValueBounds.LENGTH_ONE)
          .addFlags(ValueBounds.MAYBE_NAMES)
          .addFlags(ValueBounds.MAYBE_DIM)
          .addFlags(ValueBounds.MAYBE_DIMNAMES)
          .addFlags(ValueBounds.MAYBE_CLASS, !attributes.getBounds().isFlagSet(ValueBounds.NAME_CLASS_ABSENT))
          .addFlags(ValueBounds.MAYBE_OTHER_ATTR)
          .build();

      return new StaticMethodCall(method, resultBounds);
    }
  }
}
