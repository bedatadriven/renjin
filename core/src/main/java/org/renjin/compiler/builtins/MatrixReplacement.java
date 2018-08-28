/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
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

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;

import java.util.List;

public class MatrixReplacement implements Specialization {

  private ValueBounds resultBounds;

  public MatrixReplacement(ValueBounds sourceVector, ValueBounds[] subscriptBounds, ValueBounds replacement) {

    // First determine our result type
    ValueBounds.Builder builder = ValueBounds.builder()
        .setTypeSet(computeResultType(sourceVector, replacement));

    // Now specialize for a few specific cases
    if(isSingleElementReplaced(subscriptBounds)) {

    }

    resultBounds = sourceVector.withVaryingValues();
  }

  private boolean isSingleElementReplaced(ValueBounds[] subscriptBounds) {
    for (int i = 0; i < subscriptBounds.length; i++) {
      ValueBounds subscriptBound = subscriptBounds[i];
      if(!subscriptBound.isScalar()) {
        return false;
      }
    }
    return true;
  }

  private int computeResultType(ValueBounds sourceVector, ValueBounds replacement) {
    int sourceType = sourceVector.getTypeSet();
    int replacementType = replacement.getTypeSet();

    return TypeSet.widestVectorType(sourceType, replacementType);
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
