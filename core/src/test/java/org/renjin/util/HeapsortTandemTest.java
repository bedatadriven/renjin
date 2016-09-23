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
package org.renjin.util;

import org.junit.Assert;
import org.junit.Test;
import org.renjin.stats.internals.distributions.MersenneTwister;


public class HeapsortTandemTest {
  final int N = 1000000;
  MersenneTwister mt = new MersenneTwister(12345);

  @Test
  public void testHeapsortDescending() {
    int i = 0;
    final double[] values = new double[N];
    final int[] valuesOrder = new int[N];

    for (; i < N; i++) {
      values[i] = mt.nextDouble();
      valuesOrder[i] = i;
    }

    HeapsortTandem.heapsortDescending(values, valuesOrder, N);

    // Ensure the values are in descending order
    for (i = 0; i < N - 1; i++) {
      Assert.assertTrue(values[i] >= values[i + 1]);
    }
  }


  @Test
  public void testPresortedArrayDescending() {
    int i = 0;
    final double[] values = new double[]{0.2356849, 0.2163148, 0.1985367, 0.1822197, 0.1672438};
    final int[] valuesOrder = new int[N];

    for (; i < values.length; i++) {
      valuesOrder[i] = i;
    }

    HeapsortTandem.heapsortDescending(values, valuesOrder, values.length);

    // Ensure the values are in descending order
    for (i = 0; i < values.length-1; i++) {
      Assert.assertTrue(values[i] >= values[i + 1]);
    }
  }

}
