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


public class F {

  public static double rf(Session context, double n1, double n2) {
    double v1, v2;
    if (Double.isNaN(n1) || Double.isNaN(n2) || n1 <= 0. || n2 <= 0.) {
      return (Double.NaN);
    }


    //v1 = R_FINITE(n1) ? (rchisq(n1) / n1) : 1;
    //v2 = R_FINITE(n2) ? (rchisq(n2) / n2) : 1;

    if (Double.isInfinite(n1)) {
      v1 = 1;
    } else {
      v1 = ChiSquare.rchisq(context, n1) / n1;
    }

    if (Double.isInfinite(n2)) {
      v2 = 1;
    } else {
      v2 = ChiSquare.rchisq(context, n2) / n2;
    }

    return v1 / v2;
  }

}
