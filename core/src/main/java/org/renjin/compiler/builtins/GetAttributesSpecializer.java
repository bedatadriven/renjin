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
import org.renjin.primitives.Primitives;
import org.renjin.sexp.Null;

import java.util.List;

public class GetAttributesSpecializer implements Specializer {


  private final Primitives.Entry primitive;

  public GetAttributesSpecializer() {
    primitive = Primitives.getBuiltinEntry("attributes");
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    ArgumentBounds sexp = arguments.get(0);
    ValueBounds bounds = sexp.getBounds();

    if(bounds.hasNoAttributes()) {
      return new ConstantCall(Null.INSTANCE);

    } else {
      return new WrapperApplyCall(primitive, arguments, ValueBounds.builder()
          .setTypeSet(TypeSet.NULL | TypeSet.LIST)
          .addFlags(ValueBounds.MAYBE_NAMES)
          .addFlags(ValueBounds.NAME_CLASS_ABSENT, !bounds.isFlagSet(ValueBounds.MAYBE_CLASS))
          .build());
    }
  }
}
