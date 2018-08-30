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

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.sexp.Symbols;

import java.util.List;

/**
 * Selects a single element from within an atomic vector
 */
public class GetAtomicElement implements Specialization {

  private final ValueBounds resultBounds;

  public static GetAtomicElement trySpecialize(ValueBounds source, List<ValueBounds> subscripts) {

    if(subscripts.size() != 1) {
      return null;
    }

    ValueBounds subscript = subscripts.get(0);

    if (!TypeSet.isSpecificAtomic(source.getTypeSet())) {
      return null;
    }

    if (!subscript.isFlagSet(ValueBounds.FLAG_LENGTH_ONE)) {
      return null;
    }

    // The subscript *must* not be NA, and *must* be known to be positive, otherwise
    // we could end up with x[0] or x[-1] or x[NA], which does *not* produce a scalar
    if (!subscript.isFlagSet(ValueBounds.FLAG_NO_NA & ValueBounds.FLAG_POSITIVE)) {
      return null;
    }

    // If the source *could* have a NAME attribute
    if(source.attributeCouldBePresent(Symbols.NAME)) {
      return null;
    }

    return new GetAtomicElement(new ValueBounds.Builder()
      .setTypeSet(TypeSet.elementOf(source.getTypeSet()))
      .setFlag(ValueBounds.FLAG_LENGTH_ONE)
      .setFlagsFrom(source, ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE)
      .setEmptyAttributes()
      .build());
  }

  private GetAtomicElement(ValueBounds resultBounds) {
    this.resultBounds = resultBounds;
  }

  @Override
  public ValueBounds getResultBounds() {
    return resultBounds;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {
    throw new UnsupportedOperationException("TODO");
  }

}