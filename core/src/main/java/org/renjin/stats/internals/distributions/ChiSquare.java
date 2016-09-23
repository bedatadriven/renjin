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
/*
 * TO-DO: Do we have a renjin function for ML_ERROR ?
 */
package org.renjin.stats.internals.distributions;


import org.renjin.eval.Session;


public class ChiSquare {

  /*
   * For central chi-square distribution
   */
  static double rchisq(Session context, double df) {
    if (df < 0.0) {
      return (Double.NaN);
    }
    return Gamma.rgamma(context, df / 2.0, 2.0);
  }

  /*
   * Chi-square distribution with non-central parameter.
   */
  static double rnchisq(Session context, double df, double lambda) {
    if (df < 0. || lambda < 0.) {
      return Double.NaN;
    }

    if (lambda == 0.) {
      if (df == 0.) {
        return (Double.NaN);
      } else {
        return Gamma.rgamma(context, df / 2., 2.);
      }
    } else {
      double r = Poisson.rpois(context, lambda / 2.);
      if (r > 0.) {
        r = rchisq(context, 2. * r);
      }
      if (df > 0.) {
        r += Gamma.rgamma(context, df / 2., 2.);
      }
      return r;
    }
  }

}
