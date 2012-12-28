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

/*
 * TO-DO: Do we have a renjin function for ML_ERROR ?
 */
package org.renjin.primitives.random;


import org.renjin.eval.Session;
import org.renjin.sexp.DoubleVector;


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

  /*
   * NonCentral Chisquare Distribution Functions
   */
  public static double pnchisq(double x, double df, double ncp, boolean lower_tail, boolean log_p) {
    double ans;

    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(df) || DoubleVector.isNaN(ncp)) {
      return x + df + ncp;
    }

    if (!DoubleVector.isFinite(df) || !DoubleVector.isFinite(ncp)) {
      return DoubleVector.NaN;
    }

    if (df < 0. || ncp < 0.) {
      return DoubleVector.NaN;
    }

    ans = pnchisq_raw(x, df, ncp, 1e-12, 8 * SignRank.DBL_EPSILON, 1000000, lower_tail);
    if (ncp >= 80) {
      if (lower_tail) {
        ans = Math.min(ans, 1.0);  /* e.g., pchisq(555, 1.01, ncp = 80) */
      } else { /* !lower_tail */
        /* since we computed the other tail cancellation is likely */
        if (ans < 1e-10) {
          /*
           * Is there any function for ML_ERROR?????
           */
          //ML_ERROR(ME_PRECISION, "pnchisq");
        }
        ans = Math.max(ans, 0.0);  /* Precaution PR#7099 */
      }
    }
    if (!log_p) {
      return ans;
    }
    /* if ans is near one, we can do better using the other tail */
    if (ncp >= 80 || ans < 1 - 1e-8) {
      return Math.log(ans);
    }
    ans = pnchisq_raw(x, df, ncp, 1e-12, 8 * SignRank.DBL_EPSILON, 1000000, !lower_tail);
    return Math.log1p(-ans);
  }

  public static double pnchisq_raw(double x, double f, double theta, double errmax, double reltol, int itrmax, boolean lower_tail) {
    double lam, x2, f2, term, bound, f_x_2n, f_2n;
    double l_lam = -1., l_x = -1.; /* initialized for -Wall */
    int n;
    boolean lamSml, tSml, is_r, is_b, is_it;
    double ans, u, v, t, lt, lu = -1;

    final double _dbl_min_exp = Math.log(2) * Double.MIN_EXPONENT;
    /*= -708.3964 for IEEE double precision */

    if (x <= 0.) {
      if (x == 0. && f == 0.) {
        return lower_tail ? Math.exp(-0.5 * theta) : -Math.expm1(-0.5 * theta);
      }
      /* x < 0  or {x==0, f > 0} */
      return lower_tail ? 0. : 1.;
    }
    if (!DoubleVector.isFinite(x)) {
      return lower_tail ? 1. : 0.;
    }


    if (theta < 80) { /* use 110 for Inf, as ppois(110, 80/2, lower.tail=FALSE) is 2e-20 */
      double sum = 0, sum2 = 0, lambda = 0.5 * theta, pr = Math.exp(-lambda);
      double ans_inner;
      int i;
      /* we need to renormalize here: the result could be very close to 1 */
      for (i = 0; i < 110; pr *= lambda / ++i) {
        sum2 += pr;
        sum += pr * Distributions.pchisq(x, f + 2 * i, lower_tail, false);
        if (sum2 >= 1 - 1e-15) {
          break;
        }
      }
      ans_inner = sum / sum2;
      return ans_inner;
    }


    lam = .5 * theta;
    lamSml = (-lam < _dbl_min_exp);
    if (lamSml) {
      /* MATHLIB_ERROR(
      "non centrality parameter (= %g) too large for current algorithm",
      theta) */
      u = 0;
      lu = -lam;/* == ln(u) */
      l_lam = Math.log(lam);
    } else {
      u = Math.exp(-lam);
    }

    /* evaluate the first term */
    v = u;
    x2 = .5 * x;
    f2 = .5 * f;
    f_x_2n = f - x;



    if (f2 * SignRank.DBL_EPSILON > 0.125
            && /* very large f and x ~= f: probably needs */ Math.abs(t = x2 - f2)
            < /* another algorithm anyway */ Math.sqrt(SignRank.DBL_EPSILON) * f2) {
      /* evade cancellation error */
      /* t = exp((1 - t)*(2 - t/(f2 + 1))) / sqrt(2*M_PI*(f2 + 1));*/
      lt = (1 - t) * (2 - t / (f2 + 1)) - 0.5 * Math.log(2 * Math.PI * (f2 + 1));
    } else {
      /* Usual case 2: careful not to overflow .. : */
      lt = f2 * Math.log(x2) - x2 - org.apache.commons.math.special.Gamma.logGamma(f2 + 1);
    }

    tSml = (lt < _dbl_min_exp);
    if (tSml) {
      if (x > f + theta + 5 * Math.sqrt(2 * (f + 2 * theta))) {
        /* x > E[X] + 5* sigma(X) */
        return lower_tail ? 1. : 0.; /* FIXME: We could be more accurate than 0. */
      } /* else */
      l_x = Math.log(x);
      ans = term = t = 0.;
    } else {
      t = Math.exp(lt);
      ans = term = v * t;
    }

    for (n = 1, f_2n = f + 2., f_x_2n += 2.;; n++, f_2n += 2, f_x_2n += 2) {
      /* f_2n    === f + 2*n
       * f_x_2n  === f - x + 2*n   > 0  <==> (f+2n)  >   x */
      if (f_x_2n > 0) {
        /* find the error bound and check for convergence */

        bound = t * x / f_x_2n;
        is_r = is_it = false;
        /* convergence only if BOTH absolute and relative error < 'bnd' */
        if (((is_b = (bound <= errmax))
                && (is_r = (term <= reltol * ans))) || (is_it = (n > itrmax))) {
          break; /* out completely */
        }

      }

      /* evaluate the next term of the */
      /* expansion and then the partial sum */

      if (lamSml) {
        lu += l_lam - Math.log(n); /* u = u* lam / n */
        if (lu >= _dbl_min_exp) {
          /* no underflow anymore ==> change regime */
          v = u = Math.exp(lu); /* the first non-0 'u' */
          lamSml = false;
        }
      } else {
        u *= lam / n;
        v += u;
      }
      if (tSml) {
        lt += l_x - Math.log(f_2n);/* t <- t * (x / f2n) */
        if (lt >= _dbl_min_exp) {
          /* no underflow anymore ==> change regime */

          t = Math.exp(lt); /* the first non-0 't' */
          tSml = false;
        }
      } else {
        t *= x / f_2n;
      }
      if (!lamSml && !tSml) {
        term = v * t;
        ans += term;
      }

    } /* for(n ...) */

    if (is_it) {
      // How to alert this message without an exception?
      //(_("pnchisq(x=%g, ..): not converged in %d iter."),x, itrmax);
    }
    return lower_tail ? ans : 1 - ans;
  }

  public static double qnchisq(double p, double df, double ncp, boolean lower_tail, boolean log_p) {
    final double accu = 1e-13;
    final double racc = 4 * SignRank.DBL_EPSILON;
    /* these two are for the "search" loops, can have less accuracy: */
    final double Eps = 1e-11; /* must be > accu */
    final double rEps = 1e-10; /* relative tolerance ... */

    double ux, lx, ux0, nx, pp;


    if (DoubleVector.isNaN(p) || DoubleVector.isNaN(df) || DoubleVector.isNaN(ncp)) {
      return p + df + ncp;
    }

    if (!DoubleVector.isFinite(df)) {
      return DoubleVector.NaN;
    }

    /* Was
     * df = floor(df + 0.5);
     * if (df < 1 || ncp < 0) ML_ERR_return_NAN;
     */
    if (df < 0 || ncp < 0) {
      return DoubleVector.NaN;
    }

    //R_Q_P01_boundaries(p, 0, ML_POSINF);
    //#define R_Q_P01_boundaries(p, _LEFT_, _RIGHT_)
    //This macro is defined in /src/nmath/dpq.h
    if (log_p) {
      if (p > 0) {
        return DoubleVector.NaN;
      }
      if (p == 0) {/* upper bound*/
        return lower_tail ? Double.POSITIVE_INFINITY : 0;
      }
      if (p == Double.NEGATIVE_INFINITY) {
        return lower_tail ? 0 : Double.POSITIVE_INFINITY;
      }
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


    /* Invert pnchisq(.) :
     * 1. finding an upper and lower bound */
    {
      /* This is Pearson's (1959) approximation,
      which is usually good to 4 figs or so.  */
      double b, c, ff;
      b = (ncp * ncp) / (df + 3 * ncp);
      c = (df + 3 * ncp) / (df + 2 * ncp);
      ff = (df + 2 * ncp) / (c * c);
      ux = b + c * Distributions.qchisq(p, ff, lower_tail, log_p);
      if (ux < 0) {
        ux = 1;
      }
      ux0 = ux;
    }
    p = Normal.R_D_qIv(p, log_p ? 1.0 : 0.0);

    if (!lower_tail && ncp >= 80) {
      /* pnchisq is only for lower.tail = TRUE */
      if (p < 1e-10) {
        //ML_ERROR(ME_PRECISION, "qnchisq");
        //How to write this error message?
      }
      p = 1. - p;
      lower_tail = true;
    }

    if (lower_tail) {
      if (p > 1 - SignRank.DBL_EPSILON) {
        return Double.POSITIVE_INFINITY;
      }
      pp = Math.min(1 - SignRank.DBL_EPSILON, p * (1 + Eps));
      //This is equ to while loop!
      for (ux = ux;
              ux < Double.MAX_VALUE
              && pnchisq_raw(ux, df, ncp, Eps, rEps, 10000, true) < pp;
              ux *= 2) {
      }
      pp = p * (1 - Eps);
      for (lx = Math.min(ux0, Double.MAX_VALUE);
              lx > Double.MIN_VALUE
              && pnchisq_raw(lx, df, ncp, Eps, rEps, 10000, true) > pp;
              lx *= 0.5) {
      }
    } else {
      if (p > 1 - SignRank.DBL_EPSILON) {
        return 0.0;
      }
      pp = Math.min(1 - SignRank.DBL_EPSILON, p * (1 + Eps));
      for (ux = ux;
              ux < Double.MAX_VALUE
              && pnchisq_raw(ux, df, ncp, Eps, rEps, 10000, false) > pp;
              ux *= 2) {
      }
      pp = p * (1 - Eps);
      for (lx = Math.min(ux0, Double.MAX_VALUE);
              lx > Double.MIN_VALUE
              && pnchisq_raw(lx, df, ncp, Eps, rEps, 10000, false) < pp;
              lx *= 0.5) {
      }
    }

    /* 2. interval (lx,ux)  halving : */
    if (lower_tail) {
      do {
        nx = 0.5 * (lx + ux);
        if (pnchisq_raw(nx, df, ncp, accu, racc, 100000, true) > p) {
          ux = nx;
        } else {
          lx = nx;
        }
      } while ((ux - lx) / nx > accu);
    } else {
      do {
        nx = 0.5 * (lx + ux);
        if (pnchisq_raw(nx, df, ncp, accu, racc, 100000, false) < p) {
          ux = nx;
        } else {
          lx = nx;
        }
      } while ((ux - lx) / nx > accu);
    }
    return 0.5 * (ux + lx);
  }

  public static double dnchisq(double x, double df, double ncp, boolean give_log) {
    final double eps = 5e-15;

    double i, ncp2, q, mid, dfmid = 0.0, imax;
    double sum, term;


    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(df) || DoubleVector.isNaN(ncp)) {
      return x + df + ncp;
    }

    if (ncp < 0 || df <= 0) {
      return DoubleVector.NaN;
    }

    if (!DoubleVector.isFinite(df) || !DoubleVector.isFinite(ncp)) {
      return DoubleVector.NaN;
    }

    if (x < 0) {
      return SignRank.R_D__0(true, give_log);
    }
    if (x == 0 && df < 2.) {
      return Double.POSITIVE_INFINITY;
    }
    if (ncp == 0) {
      return Distributions.dchisq(x, df, give_log);
    }
    if (x == Double.POSITIVE_INFINITY) {
      return SignRank.R_D__0(true, give_log);
    }

    ncp2 = 0.5 * ncp;

    /* find max element of sum */
    imax = Math.ceil((-(2 + df) + Math.sqrt((2 - df) * (2 - df) + 4 * ncp * x)) / 4);
    if (imax < 0) {
      imax = 0;
    }
    if (DoubleVector.isFinite(imax)) {
      dfmid = df + 2 * imax;
      mid = Poisson.dpois_raw(imax, ncp2, false) * Distributions.dchisq(x, dfmid, false);
    } else /* imax = Inf */ {
      mid = 0;
    }

    if (mid == 0) {
      /* underflow to 0 -- maybe numerically correct; maybe can be more accurate,
       * particularly when  give_log = TRUE */
      /* Use  central-chisq approximation formula when appropriate;
       * ((FIXME: the optimal cutoff also depends on (x,df);  use always here? )) */
      if (give_log || ncp > 1000.) {
        double nl = df + ncp, ic = nl / (nl + ncp);/* = "1/(1+b)" Abramowitz & St.*/
        return Distributions.dchisq(x * ic, nl * ic, give_log);
      } else {
        return SignRank.R_D__0(true, give_log);
      }
    }

    sum = mid;

    /* errorbound := term * q / (1-q)  now subsumed in while() / if() below: */

    /* upper tail */
    term = mid;
    df = dfmid;
    i = imax;
    double x2 = x * ncp2;
    do {
      i++;
      q = x2 / i / df;
      df += 2;
      term *= q;
      sum += term;
    } while (q >= 1 || term * q > (1 - q) * eps || term > 1e-10 * sum);
    /* lower tail */
    term = mid;
    df = dfmid;
    i = imax;
    while (i >= 0) {
      df -= 2;
      q = i * df / x2;
      i--;
      term *= q;
      sum += term;
      if (q < 1 && term * q <= (1 - q) * eps) {
        break;
      }
    }
    return SignRank.R_D_val(sum, true, give_log);
  }
}
