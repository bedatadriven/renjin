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
package org.renjin.gcc.runtime;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.gcc.runtime.DoubleMatchers.isNegativeZero;
import static org.renjin.gcc.runtime.DoubleMatchers.isPositiveZero;


public class RintTest {

  @Test
  public void zero() {
    assertThat(Mathlib.rint(0.0), isPositiveZero());
    assertThat(Mathlib.rint(-0.0), isNegativeZero());
  }
  
  @Test
  public void positive() {
    assertThat(Mathlib.rint(0.200000), equalTo(0.0));
    assertThat(Mathlib.rint(0.500000), equalTo(0.0));
    assertThat(Mathlib.rint(0.700000), equalTo(1.0));
    assertThat(Mathlib.rint(1.0), equalTo(1.0));
    assertThat(Mathlib.rint(2.300000), equalTo(2.0));
    assertThat(Mathlib.rint(2.500000), equalTo(2.0));
    assertThat(Mathlib.rint(2.700000), equalTo(3.0));
  }
  
  @Test
  public void negativeNumbers() {
    assertThat(Mathlib.rint(-0.2), equalTo(-0.0));
    assertThat(Mathlib.rint(-0.5), equalTo(-0.0));
    assertThat(Mathlib.rint(-0.75), equalTo(-1.0));
    assertThat(Mathlib.rint(-2.3), equalTo(-2.0));
    assertThat(Mathlib.rint(-2.5), equalTo(-2.0));
    assertThat(Mathlib.rint(-2.7), equalTo(-3.0));
  }

}
