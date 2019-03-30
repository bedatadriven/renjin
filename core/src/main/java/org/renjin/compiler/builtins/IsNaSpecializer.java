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

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.List;

public class IsNaSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
    ArgumentBounds sexp = arguments.get(0);

    ValueBounds bounds = ValueBounds.builder()
        .addFlags(ValueBounds.FLAG_NO_NA)
        .addFlagsFrom(sexp.getBounds(), ValueBounds.LENGTH_ONE | ValueBounds.LENGTH_NON_ZERO)
        .addFlagsFrom(sexp.getBounds(), ValueBounds.MAYBE_ATTRIBUTES | ValueBounds.HAS_DIM)
        .build();

    return new Specialization() {
      @Override
      public ValueBounds getResultBounds() {
        return bounds;
      }

      @Override
      public boolean isPure() {
        return true;
      }

      @Override
      public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {
        throw new UnsupportedOperationException("TODO");
      }
    };
  }
}
