/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.renjin.primitives.random;

import org.renjin.eval.Session;


public class Exponantial {

  final static double q[] =
          new double[]{
    0.6931471805599453,
    0.9333736875190459,
    0.9888777961838675,
    0.9984959252914960,
    0.9998292811061389,
    0.9999833164100727,
    0.9999985691438767,
    0.9999998906925558,
    0.9999999924734159,
    0.9999999995283275,
    0.9999999999728814,
    0.9999999999985598,
    0.9999999999999289,
    0.9999999999999968,
    0.9999999999999999,
    1.0000000000000000
  };

  public static double exp_rand(Session context) {
    double a = 0.;
    double u = context.rng.unif_rand();    /* precaution if u = 0 is ever returned */
    while (u <= 0. || u >= 1.) {
      u = context.rng.unif_rand();
    }
    for (;;) {
      u += u;
      if (u > 1.) {
        break;
      }
      a += q[0];
    }
    u -= 1.;

    if (u <= q[0]) {
      return a + u;
    }

    int i = 0;
    double ustar = context.rng.unif_rand(), umin = ustar;
    do {
      ustar = context.rng.unif_rand();
      if (umin > ustar) {
        umin = ustar;
      }
      i++;
    } while (u > q[i]);
    return a + umin * q[0];
  }

  public static double rexp(Session context, double scale) {
    if (scale <= 0.0) {
      if (scale == 0.) {
        return 0.;
      } else {
        return Double.NaN;
      }
    }
    return scale * Exponantial.exp_rand(context);
  }
}
