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
package org.renjin.primitives;


import org.junit.Test;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MathGroupTest {

  @Test
  public void roundTest() {
    assertThat(MathGroup.round(7.0, 20), equalTo(7.0));
    assertThat(MathGroup.round(7.0, 1), equalTo(7.0));
    assertThat(MathGroup.round(7.3234324, 0), equalTo(7.0));
    assertThat(MathGroup.round(1d/3d, 5), equalTo(0.33333));
    
    assertThat(MathGroup.round(7.*Math.pow(10,20)), equalTo(7e20));
  }
  
  @Test
  public void signifTest() {
    assertThat(MathGroup.signif(Math.PI, 2), equalTo(3.1d));
    assertThat(MathGroup.signif(Math.PI, 3), equalTo(3.14d));
    assertThat(MathGroup.signif(Math.PI, 1), equalTo(3.0d));
    assertThat(MathGroup.signif(Math.PI, 0), equalTo(3.0d));
    assertThat(MathGroup.signif(Math.PI, -1), equalTo(3.0d));
  }

  @Test
  public void signifInfiniteTest() {
    assertThat(MathGroup.signif(Double.POSITIVE_INFINITY, 2), equalTo(Double.POSITIVE_INFINITY));
    assertThat(MathGroup.signif(Double.NEGATIVE_INFINITY, 2), equalTo(Double.NEGATIVE_INFINITY));
    assertTrue(DoubleVector.isNA(MathGroup.signif(DoubleVector.NA, 3)));
  }
}
