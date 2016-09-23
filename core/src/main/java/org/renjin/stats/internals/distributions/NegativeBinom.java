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
