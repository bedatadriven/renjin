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
package org.renjin.primitives;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArrayTest {


  @Test
  public void arrayToVector() {

    assertThat( Indexes.arrayIndexToVectorIndex(index(0, 1, 0), dim(2, 3, 4)), equalTo(2) );
    assertThat( Indexes.arrayIndexToVectorIndex(index(1, 2, 2), dim(2, 3, 4)), equalTo(17) );
    assertThat( Indexes.arrayIndexToVectorIndex(index(6, 1), dim(8, 2)), equalTo(14) );

  }

  @Test
  public void vectorToArray() {
    assertThat( Indexes.vectorIndexToArrayIndex(17, dim(2, 3, 4)), equalTo(index(1,2,2)));
  }

  private int[] dim(int... values) {
    return values;
  }

  private int[] index(int... values) {
    return values;
  }


}
