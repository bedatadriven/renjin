/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DeferredColSumsTest {

  @Test
  public void test() {

    //        [,1] [,2] [,3]
    //  [1,]    1    5    9
    //  [2,]    2    6   10
    //  [3,]    3    7   11
    //  [4,]    4    8   12
    DeferredColSums colSums = new DeferredColSums(new IntSequence(1, 1, 12), 3, false, AttributeMap.EMPTY);
    Vector vector = colSums.forceResult();

    assertThat(vector.length(), equalTo(3));
    assertThat(vector.getElementAsDouble(0), equalTo(10d));
    assertThat(vector.getElementAsDouble(1), equalTo(26d));
    assertThat(vector.getElementAsDouble(2), equalTo(42d));
  }
}