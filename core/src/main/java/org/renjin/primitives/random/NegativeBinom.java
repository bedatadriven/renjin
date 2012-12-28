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

public class NegativeBinom {

  public static double rnbinom(Session context, double size, double prob) {
    if (Double.isInfinite(size) || Double.isInfinite(prob) || size <= 0 || prob <= 0 || prob > 1) {
      /* prob = 1 is ok, PR#1218 */
      return (Double.NaN);
    }
    return (prob == 1) ? 0 : Poisson.rpois(context, Gamma.rgamma(context, size, (1 - prob) / prob));
  }

  public static double rnbinom_mu(Session context, double size, double mu) {
    if (Double.isInfinite(size) || Double.isInfinite(mu) || size <= 0 || mu < 0) {
      return (Double.NaN);
    }
    return (mu == 0) ? 0 : Poisson.rpois(context, Gamma.rgamma(context, size, mu / size));
  }
}
