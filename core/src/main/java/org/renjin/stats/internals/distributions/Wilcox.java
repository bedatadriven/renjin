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
package org.renjin.stats.internals.distributions;

import org.renjin.eval.Session;


public class Wilcox {

  public static double rwilcox(Session context, double m, double n) {
    int i, j, k;
    int[] x;
    double r;


    /* NaNs propagated correctly */
    if (Double.isNaN(m) || Double.isNaN(n)) {
      return (m + n);
    }

    m = Math.floor(m + 0.5);
    n = Math.floor(n + 0.5);
    if ((m < 0) || (n < 0)) {
      return Double.NaN;
    }

    if ((m == 0) || (n == 0)) {
      return (0);
    }

    r = 0.0;
    k = (int) (m + n);
    x = new int[k];
    for (i = 0; i < k; i++) {
      x[i] = i;
    }

    for (i = 0; i < n; i++) {
      j = (int) (Math.floor(k * context.rng.unif_rand()));
      r += x[j];
      x[j] = x[--k];
    }
    return (r - n * (n - 1) / 2);
  }


}
