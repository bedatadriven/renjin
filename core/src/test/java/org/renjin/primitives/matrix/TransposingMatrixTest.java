/**
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
package org.renjin.primitives.matrix;

import org.junit.Test;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.Symbols;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class TransposingMatrixTest {

  @Test
  public void transposed() {
    DoubleVector x = new DoubleSequence(1,1,12);
    x = (DoubleVector)x.setAttribute(Symbols.DIM, new IntArrayVector(4,3));

    DoubleVector y = new TransposingMatrix(x, AttributeMap.dim(3, 4));
    assertThat(y.getElementAsDouble(9), equalTo(4d));
    assertThat(y.getElementAsDouble(4), equalTo(6d));
  }
}
