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


public class Geometric {

  public static double rgeom(Session context, double p) {
    if (p <= 0 || p > 1) {
      return (Double.NaN);
    }

    return Poisson.rpois(context, Exponantial.exp_rand(context) * ((1 - p) / p));
  }

  public static double pgeom(double x, double p, boolean lower_tail, boolean log_p) {

    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(p)) {
      return x + p;
    }

    if (p <= 0 || p > 1) {
      return DoubleVector.NaN;
    }

    if (x < 0.) {
      return SignRank.R_DT_0(lower_tail, log_p);
    }

    if (!DoubleVector.isFinite(x)) {
      return SignRank.R_DT_1(lower_tail, log_p);
    }

    x = Math.floor(x + 1e-7);

    if (p == 1.) { /* we cannot assume IEEE */
      x = lower_tail ? 1 : 0;
      return log_p ? Math.log(x) : x;
    }
    x = Math.log1p(-p) * (x + 1);
    if (log_p) {
      return SignRank.R_DT_Clog(x, lower_tail, log_p);
    } else {
      return lower_tail ? -Math.expm1(x) : Math.exp(x);
    }
  }

  public static double dgeom(double x, double p, boolean give_log) {
    double prob;


    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(p)) {
      return x + p;
    }


    if (p <= 0 || p > 1) {
      return DoubleVector.NaN;
    }

    if (SignRank.R_D_nonint(x, true, give_log)) {
      return SignRank.R_D__0(true, give_log);
    }

    if (x < 0 || !DoubleVector.isFinite(x) || p == 0) {
      return SignRank.R_D__0(true, give_log);
    }
    x = SignRank.R_D_forceint(x);

    /* prob = (1-p)^x, stable for small p */
    prob = Binom.dbinom_raw(0., x, p, 1 - p, give_log);

    return ((give_log) ? Math.log(p) + prob : p * prob);
  }

  public static double qgeom(double p, double prob, boolean lower_tail, boolean log_p) {
    if (prob <= 0 || prob > 1) {
      return DoubleVector.NaN;
    }

    //R_Q_P01_boundaries(p, 0, Double.POSITIVE_INFINITY);
    if (log_p) {
      if (p > 0) {
        return DoubleVector.NaN;
      }
      if (p == 0) /* upper bound*/ {
        return lower_tail ? Double.POSITIVE_INFINITY : 0;
      }
      if (p == Double.NEGATIVE_INFINITY);
      return lower_tail ? 0 : Double.POSITIVE_INFINITY;
    } else { /* !log_p */
      if (p < 0 || p > 1) {
        return DoubleVector.NaN;
      }
      if (p == 0) {
        return lower_tail ? 0 : Double.POSITIVE_INFINITY;
      }
      if (p == 1) {
        return lower_tail ? Double.POSITIVE_INFINITY : 0;
      }
    }


    if (DoubleVector.isNaN(p) || DoubleVector.isNaN(prob)) {
      return p + prob;
    }

    if (prob == 1) {
      return (0);
    }
    /* add a fuzz to ensure left continuity */
    return Math.ceil(SignRank.R_DT_Clog(p, lower_tail, log_p) / Math.log1p(-prob) - 1 - 1e-7);
  }
}
