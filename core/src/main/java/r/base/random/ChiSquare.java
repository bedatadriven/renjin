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
package r.base.random;

public class ChiSquare {

  /*
   * For central chi-square distribution
   */
  static double rchisq(double df) {
    if (df < 0.0) {
      return (Double.NaN);
    }
    return Gamma.rgamma(df / 2.0, 2.0);
  }

  /*
   * Chi-square distribution with non-central parameter.
   */
  static double rnchisq(double df, double lambda) {
    if (df < 0. || lambda < 0.) {
      return Double.NaN;
    }

    if (lambda == 0.) {
      if (df == 0.) {
        return (Double.NaN);
      } else {
        return Gamma.rgamma(df / 2., 2.);
      }
    } else {
      double r = Poisson.rpois(lambda / 2.);
      if (r > 0.) {
        r = rchisq(2. * r);
      }
      if (df > 0.) {
        r += Gamma.rgamma(df / 2., 2.);
      }
      return r;
    }
  }
}
