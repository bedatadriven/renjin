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
package org.renjin.gnur;

import org.junit.Test;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.renjin.gnur.RenjinCApi.R_pow_di;

public class RenjinCApiTest {

  @Test
  public void pow_di() {
    assertThat(R_pow_di(0d, 1), equalTo(0d));
    assertTrue(DoubleVector.isNA(R_pow_di(DoubleVector.NA, 41)));

    assertThat(R_pow_di(2d, 16), equalTo(65536d));
    assertThat(R_pow_di(2d, -16), closeTo(1.525879e-05, 0.0001));
    assertThat(R_pow_di(3d, 7), equalTo(2187d));
    assertThat(R_pow_di(3d, -7), closeTo(0.0004572474d, 0.000000001));
  }
}