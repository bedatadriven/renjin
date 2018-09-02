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
package org.renjin.primitives.subset;

import org.junit.Test;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Symbol;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MatrixSelectionTest {

  @Test
  public void testBoundsCalculations() {

    // Define a matrix that looks something like
    // m <- matrix(1:12, nrow = 3)

    ValueBounds m = ValueBounds.builder()
        .setTypeSet(TypeSet.DOUBLE)
        .setDimCount(2)
        .closeAttributes()
        .build();

    // Define a few subscripts
    ValueBounds scalar = ValueBounds.primitive(TypeSet.INT);
    ValueBounds missing = ValueBounds.of(Symbol.MISSING_ARG);
    ValueBounds trueScalar = ValueBounds.of(LogicalVector.TRUE);
    ValueBounds falseScalar = ValueBounds.of(LogicalVector.FALSE);

    // Even if we have an operation like m[i, ] where i is known to be a scalar,
    // we *still* don't know what the resulting dimensions will be.
    // m[0, ] => matrix(integer(0), ncol=4)
    // m[1, ] => c(1, 4, 7, 10)

    assertThat(MatrixSelection.computeResultBounds(m, Arrays.asList(scalar, missing), true).toString(),
        equalTo("[double, len=*, ?NA, dim=?]"));
  }
}