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
import org.renjin.sexp.DoubleVector;


public class LNorm {

  public static double rlnorm(Session context, double meanlog, double sdlog) {
    if (Double.isNaN(meanlog) || sdlog < 0.) {
      return (Double.NaN);
    }

    return Math.exp(Normal.rnorm(context, meanlog, sdlog));
  }

  public static double dlnorm(double x, double meanlog, double sdlog, boolean give_log) {
    double y;


    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(meanlog) || DoubleVector.isNaN(sdlog)) {
      return x + meanlog + sdlog;
    }

    if (sdlog <= 0) {
      return DoubleVector.NaN;
    }

    //if(x <= 0) return R_D__0;
    if (x <= 0) {
      return SignRank.R_D__0(true, give_log);
    }

    y = (Math.log(x) - meanlog) / sdlog;
    return (give_log ? -(Math.log(Math.sqrt(2 * Math.PI)) + 0.5 * y * y + Math.log(x * sdlog))
            : (1 / Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * y * y) / (x * sdlog));
    /* M_1_SQRT_2PI = 1 / sqrt(2 * pi) */

  }
}
