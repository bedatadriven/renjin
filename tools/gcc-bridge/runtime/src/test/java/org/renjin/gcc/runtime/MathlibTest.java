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
package org.renjin.gcc.runtime;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MathlibTest {

  @Test
  public void testFloatingPointModulus() {
    // This should be EXACT EXACT
    assertTrue(Mathlib.fmod(1.5, 1.) == 0.5);
  }

  @Test
  public void modf() {
    checkModf(3.145, 3, 0.145);
    checkModf(-3.145, -3, -0.145);
  }

  private void checkModf(double x, double expectedInteger, double expectedFraction) {
    DoublePtr intValue = new DoublePtr(0);
    double fracValue = Mathlib.modf(x, intValue);

    assertThat(intValue.get(), equalTo(expectedInteger));
    assertThat(fracValue, closeTo(expectedFraction, 0.00001));
  }
}