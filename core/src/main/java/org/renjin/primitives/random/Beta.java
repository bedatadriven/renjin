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

import org.apache.commons.math.special.Gamma;
import org.renjin.eval.Session;
import org.renjin.primitives.annotations.Current;
import org.renjin.sexp.DoubleVector;

public class Beta {

  public static double expmax = (Float.MAX_EXPONENT * Math.log(2)); /* = log(DBL_MAX) */

  /* FIXME:  Keep Globals (properly) for threading */
  /* Uses these GLOBALS to save time when many rv's are generated : */
  static double beta, gamma, delta, k1, k2;
  static double olda = -1.0;
  static double oldb = -1.0;

  /*
  #define v_w_from__u1_bet(AA) 			\
  v = beta * log(u1 / (1.0 - u1));	\
  if (v <= expmax) {			\
  w = AA * exp(v);		\
  if(!R_FINITE(w)) w = DBL_MAX;	\
  } else				\
  w = DBL_MAX
   */
  public static double rbeta(Session context, double aa, double bb) {
    double a, b, alpha;
    double r, s, t, u1, u2, v, w, y, z;

    boolean qsame;


    if (aa <= 0. || bb <= 0. || (Double.isInfinite(aa) && Double.isInfinite(bb))) {
      return (Double.NaN);
    }

    if (Double.isInfinite(aa)) {
      return 1.0;
    }

    if (Double.isInfinite(bb)) {
      return 0.0;
    }

    /* Test if we need new "initializing" */
    qsame = (olda == aa) && (oldb == bb);
    if (!qsame) {
      olda = aa;
      oldb = bb;
    }

    a = Math.min(aa, bb);
    b = Math.max(aa, bb); /* a <= b */
    alpha = a + b;


    if (a <= 1.0) {	/* --- Algorithm BC --- */

      /* changed notation, now also a <= b (was reversed) */

      if (!qsame) { /* initialize */
        beta = 1.0 / a;
        delta = 1.0 + b - a;
        k1 = delta * (0.0138889 + 0.0416667 * a) / (b * beta - 0.777778);
        k2 = 0.25 + (0.5 + 0.25 / delta) * a;
      }
      /* FIXME: "do { } while()", but not trivially because of "continue"s:*/
      for (;;) {
        u1 = context.rng.unif_rand();
        u2 = context.rng.unif_rand();
        if (u1 < 0.5) {
          y = u1 * u2;
          z = u1 * y;
          if (0.25 * u2 + z - y >= k1) {
            continue;
          }
        } else {
          z = u1 * u1 * u2;
          if (z <= 0.25) {
            v = beta * Math.log(u1 / (1.0 - u1));
            if (v <= expmax) {
              w = b * Math.exp(v);
              if (Double.isInfinite(w)) {
                w = Double.MAX_VALUE;
              }
            } else {
              w = Double.MAX_VALUE;
            }
            break;
          }
          if (z >= k2) {
            continue;
          }
        }

        v = beta * Math.log(u1 / (1.0 - u1));
        if (v <= expmax) {
          w = b * Math.exp(v);
          if (Double.isInfinite(w)) {
            w = Double.MAX_VALUE;
          }
        } else {
          w = Double.MAX_VALUE;
        }

        if (alpha * (Math.log(alpha / (a + w)) + v) - 1.3862944 >= Math.log(z)) {
          break;
        }
      }
      return (aa == a) ? a / (a + w) : w / (a + w);

    } else {		/* Algorithm BB */

      if (!qsame) { /* initialize */
        beta = Math.sqrt((alpha - 2.0) / (2.0 * a * b - alpha));
        gamma = a + 1.0 / beta;
      }
      do {
        u1 = context.rng.unif_rand();
        u2 = context.rng.unif_rand();

        v = beta * Math.log(u1 / (1.0 - u1));
        if (v <= expmax) {
          w = a * Math.exp(v);
          if (Double.isInfinite(w)) {
            w = Double.MAX_VALUE;
          }
        } else {
          w = Double.MAX_VALUE;
        }

        z = u1 * u1 * u2;
        r = gamma * v - 1.3862944;
        s = a + r - w;
        if (s + 2.609438 >= 5.0 * z) {
          break;
        }
        t = Math.log(z);
        if (s > t) {
          break;
        }
      } while (r + alpha * Math.log(alpha / (b + w)) < t);

      return (aa != a) ? b / (b + w) : w / (b + w);
    }
  }

  public static double dnbeta(double x, double a, double b, double ncp, boolean give_log) {
    final double eps = 1.e-15;

    int kMax;
    double k, ncp2, dx2, d, D;
    double sum, term, p_k, q; /* They were LDOUBLE */


    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(a) || DoubleVector.isNaN(b) || DoubleVector.isNaN(ncp)) {
      return x + a + b + ncp;
    }

    if (ncp < 0 || a <= 0 || b <= 0) {
      return DoubleVector.NaN;
    }

    if (!DoubleVector.isFinite(a) || !DoubleVector.isFinite(b) || !DoubleVector.isFinite(ncp)) {
      return DoubleVector.NaN;
    }

    if (x < 0 || x > 1) {
      return (SignRank.R_D__0(true, give_log));
    }

    if (ncp == 0) {
      return Distributions.dbeta(x, a, b, give_log);
    }

    /* New algorithm, starting with *largest* term : */
    ncp2 = 0.5 * ncp;
    dx2 = ncp2 * x;
    d = (dx2 - a - 1) / 2;
    D = d * d + dx2 * (a + b) - a;
    if (D <= 0) {
      kMax = 0;
    } else {
      D = Math.ceil(d + Math.sqrt(D));
      kMax = (D > 0) ? (int) D : 0;
    }

    /* The starting "middle term" --- first look at it's log scale: */
    term = Distributions.dbeta(x, a + kMax, b, /* log = */ true);
    p_k = Poisson.dpois_raw(kMax, ncp2, true);
    if (x == 0. || !DoubleVector.isFinite(term) || !DoubleVector.isFinite(p_k)) /* if term = +Inf */ {
      return SignRank.R_D_exp(p_k + term, true, give_log);
    }

    /* Now if s_k := p_k * t_k  {here = exp(p_k + term)} would underflow,
     * we should rather scale everything and re-scale at the end:*/

    p_k += term; /* = log(p_k) + log(t_k) == log(s_k) -- used at end to rescale */
    /* mid = 1 = the rescaled value, instead of  mid = exp(p_k); */

    /* Now sum from the inside out */
    sum = term = 1. /* = mid term */;
    /* middle to the left */
    k = kMax;
    while (k > 0 && term > sum * eps) {
      k--;
      q = /* 1 / r_k = */ (k + 1) * (k + a) / (k + a + b) / dx2;
      term *= q;
      sum += term;
    }
    /* middle to the right */
    term = 1.;
    k = kMax;
    do {
      q = /* r_{old k} = */ dx2 * (k + a + b) / (k + a) / (k + 1);
      k++;
      term *= q;
      sum += term;
    } while (term > sum * eps);

    return SignRank.R_D_exp(p_k + Math.log(sum), true, give_log);
  }

  public static double pnbeta_raw(double x, double o_x, double a, double b, double ncp) {
    /* o_x  == 1 - x  but maybe more accurate */

    /* change errmax and itrmax if desired;
     * original (AS 226, R84) had  (errmax; itrmax) = (1e-6; 100) */
    final double errmax = 1.0e-9;
    final int itrmax = 10000;  /* 100 is not enough for pf(ncp=200)
    see PR#11277 */

    double[] temp = new double[1];
    double[] tmp_c = new double[1];
    int[] ierr = new int[1];
    double a0, ax, lbeta, c, errbd, x0;
    int j;

    double ans, gx, q, sumq;

    if (ncp < 0. || a <= 0. || b <= 0.) {
      return DoubleVector.NaN;
    }

    if (x < 0. || o_x > 1. || (x == 0. && o_x == 1.)) {
      return 0.;
    }
    if (x > 1. || o_x < 0. || (x == 1. && o_x == 0.)) {
      return 1.;
    }

    c = ncp / 2.;

    /* initialize the series */

    x0 = Math.floor(Math.max(c - 7. * Math.sqrt(c), 0.));
    a0 = a + x0;
    lbeta = org.apache.commons.math.special.Gamma.logGamma(a0) + org.apache.commons.math.special.Gamma.logGamma(b) - org.apache.commons.math.special.Gamma.logGamma(a0 + b);
    /* temp = pbeta_raw(x, a0, b, TRUE, FALSE), but using (x, o_x): */
    Utils.bratio(a0, b, x, o_x, temp, tmp_c, ierr, false);

    gx = Math.exp(a0 * Math.log(x) + b * (x < .5 ? Math.log1p(-x) : Math.log(o_x))
            - lbeta - Math.log(a0));
    if (a0 > a) {
      q = Math.exp(-c + x0 * Math.log(c) - org.apache.commons.math.special.Gamma.logGamma(x0 + 1.));
    } else {
      q = Math.exp(-c);
    }

    sumq = 1. - q;
    ans = ax = q * temp[0];

    /* recurse over subsequent terms until convergence is achieved */
    j = (int) x0;
    do {
      j++;
      temp[0] -= gx;
      gx *= x * (a + b + j - 1.) / (a + j);
      q *= c / j;
      sumq -= q;
      ax = temp[0] * q;
      ans += ax;
      errbd = (temp[0] - gx) * sumq;
    } while (errbd > errmax && j < itrmax + x0);

    if (errbd > errmax) {
      //ML_ERROR(ME_PRECISION, "pnbeta");
    }
    if (j >= itrmax + x0) {
      //ML_ERROR(ME_NOCONV, "pnbeta");
    }

    return ans;
  }

  public static double pnbeta2(double x, double o_x, double a, double b, double ncp, boolean lower_tail, boolean log_p) {
    double ans = pnbeta_raw(x, o_x, a, b, ncp);
    /* return R_DT_val(ans), but we want to warn about cancellation here */
    if (lower_tail) {
      return log_p ? Math.log(ans) : ans;
    } else {
      if (ans > 1 - 1e-10) {
        return (DoubleVector.NaN);
      }
      ans = Math.min(ans, 1.0);  /* Precaution */
      return log_p ? Math.log1p(-ans) : (1 - ans);
    }
  }

  public static double pnbeta(double x, double a, double b, double ncp, boolean lower_tail, boolean log_p) {

    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(a) || DoubleVector.isNaN(b) || DoubleVector.isNaN(ncp)) {
      return x + a + b + ncp;
    }


    //R_P_bounds_01(x, 0., 1.);
    if (x <= 0.0) {
      return SignRank.R_DT_0(lower_tail, log_p);
    }
    if (x >= 1.0) {
      return SignRank.R_DT_1(lower_tail, log_p);
    }

    return pnbeta2(x, 1 - x, a, b, ncp, lower_tail, log_p);
  }

  public static double qnbeta(double p, double a, double b, double ncp,
          boolean lower_tail, boolean log_p) {
    final double accu = 1e-15;
    final double Eps = 1e-14; /* must be > accu */

    double ux, lx, nx, pp;


    if (DoubleVector.isNaN(p) || DoubleVector.isNaN(a) || DoubleVector.isNaN(b) || DoubleVector.isNaN(ncp)) {
      return p + a + b + ncp;
    }

    if (!DoubleVector.isFinite(a)) {
      return DoubleVector.NaN;
    }

    if (ncp < 0. || a <= 0. || b <= 0.) {
      return DoubleVector.NaN;
    }

    //R_Q_P01_boundaries(p, 0, 1);
    if ((log_p && p > 0) || (!log_p && (p < 0 || p > 1))) {
      return DoubleVector.NaN;
    }
    if (p == SignRank.R_DT_0(lower_tail, log_p)) {
      return 0.0;
    }
    if (p == SignRank.R_DT_1(lower_tail, log_p)) {
      return 1.0;
    }
    //end of R_Q_P01_boundaries

    p = Normal.R_DT_qIv(p, log_p ? 1.0 : 0.0, lower_tail ? 1.0 : 0.0);

    /* Invert pnbeta(.) :
     * 1. finding an upper and lower bound */
    if (p > 1 - SignRank.DBL_EPSILON) {
      return 1.0;
    }
    pp = Math.min(1 - SignRank.DBL_EPSILON, p * (1 + Eps));
    for (ux = 0.5;
            ux < 1 - SignRank.DBL_EPSILON && pnbeta(ux, a, b, ncp, true, false) < pp;
            ux = 0.5 * (1 + ux));
    pp = p * (1 - Eps);
    for (lx = 0.5;
            lx > Double.MIN_VALUE && pnbeta(lx, a, b, ncp, true, false) > pp;
            lx *= 0.5);

    /* 2. interval (lx,ux)  halving : */
    do {
      nx = 0.5 * (lx + ux);
      if (pnbeta(nx, a, b, ncp, true, false) > p) {
        ux = nx;
      } else {
        lx = nx;
      }
    } while ((ux - lx) / nx > accu);

    return 0.5 * (ux + lx);
  }
}