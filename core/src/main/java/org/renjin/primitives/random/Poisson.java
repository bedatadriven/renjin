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
import org.renjin.sexp.DoubleVector;

public class Poisson {

  static double a0 = -0.5;
  static double a1 = 0.3333333;
  static double a2 = -0.2500068;
  static double a3 = 0.2000118;
  static double a4 = -0.1661269;
  static double a5 = 0.1421878;
  static double a6 = -0.1384794;
  static double a7 = 0.1250060;
  static double one_7 = 0.1428571428571428571;
  static double one_12 = 0.0833333333333333333;
  static double one_24 = 0.0416666666666666667;
  /*
   * factorial table 
   */
  final static double[] fact = new double[]{
    1., 1., 2., 6., 24., 120., 720., 5040., 40320., 362880.
  };
  static int l, m;
  static double b1, b2, c, c0, c1, c2, c3;
  static double[] pp = new double[36];
  static double p0, p, q, s, d, omega;
  static double big_l;/* integer "w/o overflow" */

  static double muprev = 0., muprev2 = 0.;/*, muold	 = 0.*/

  static double M_1_SQRT_2PI = 0.398942280401432677939946059934; /* 1/sqrt(2pi) */


  public static double fsign(double x, double y) {
    return ((y >= 0) ? Math.abs(x) : Math.abs(x));
  }

  public static double rpois(Session context, double mu) {

    /* Local Vars  [initialize some for -Wall]: */
    double del, difmuk = 0., E = 0., fk = 0., fx, fy, g, px, py, t, u = 0., v, x;
    double pois = -1.;

    int k, kflag = 0;
    boolean big_mu;
    boolean new_big_mu = false;

    boolean gotoStepF = false;

    if (mu < 0) {
      return (Double.NaN);
    }

    if (mu <= 0.) {
      return 0.;
    }

    big_mu = (mu >= 10.);
    if (big_mu) {
      new_big_mu = false;
    }

    if (!(big_mu && mu == muprev)) {/* maybe compute new persistent par.s */

      if (big_mu) {
        new_big_mu = true;
        /* Case A. (recalculation of s,d,l	because mu has changed):
         * The poisson probabilities pk exceed the discrete normal
         * probabilities fk whenever k >= m(mu).
         */
        muprev = mu;
        s = Math.sqrt(mu);
        d = 6. * mu * mu;
        big_l = Math.floor(mu - 1.1484);
        /* = an upper bound to m(mu) for all mu >= 10.*/
      } else { /* Small mu ( < 10) -- not using normal approx. */

        /* Case B. (start new table and calculate p0 if necessary) */

        /*muprev = 0.;-* such that next time, mu != muprev ..*/
        if (mu != muprev) {
          muprev = mu;
          m = Math.max(1, (int) mu);
          l = 0; /* pp[] is already ok up to pp[l] */
          q = p0 = p = Math.exp(-mu);
        }

        for (;;) {
          /* Step U. uniform sample for inversion method */
          u = context.rng.unif_rand();
          if (u <= p0) {
            return 0.;
          }

          /* Step T. table comparison until the end pp[l] of the
          pp-table of cumulative poisson probabilities
          (0.458 > ~= pp[9](= 0.45792971447) for mu=10 ) */
          if (l != 0) {
            for (k = (u <= 0.458) ? 1 : Math.min(l, m); k <= l; k++) {
              if (u <= pp[k]) {
                return (double) k;
              }
            }
            if (l == 35) /* u > pp[35] */ {
              continue;
            }
          }
          /* Step C. creation of new poisson
          probabilities p[l..] and their cumulatives q =: pp[k] */
          l++;
          for (k = l; k <= 35; k++) {
            p *= mu / k;
            q += p;
            pp[k] = q;
            if (u <= q) {
              l = k;
              return (double) k;
            }
          }
          l = 35;
        } /* end(repeat) */
      }/* mu < 10 */

    } /* end {initialize persistent vars} */

    /* Only if mu >= 10 : ----------------------- */

    /* Step N. normal sample */
    g = mu + s * Normal.norm_rand(context);/* norm_rand() ~ N(0,1), standard normal */

    if (g >= 0.) {
      pois = Math.floor(g);
      /* Step I. immediate acceptance if pois is large enough */
      if (pois >= big_l) {
        return pois;
      }
      /* Step S. squeeze acceptance */
      fk = pois;
      difmuk = mu - fk;
      u = context.rng.unif_rand(); /* ~ U(0,1) - sample */
      if (d * u >= difmuk * difmuk * difmuk) {
        return pois;
      }
    }

    /* Step P. preparations for steps Q and H.
    (recalculations of parameters if necessary) */

    if (new_big_mu || mu != muprev2) {
      /* Careful! muprev2 is not always == muprev
      because one might have exited in step I or S
       */
      muprev2 = mu;
      omega = M_1_SQRT_2PI / s;
      /* The quantities b1, b2, c3, c2, c1, c0 are for the Hermite
       * approximations to the discrete normal probabilities fk. */

      b1 = one_24 / mu;
      b2 = 0.3 * b1 * b1;
      c3 = one_7 * b1 * b2;
      c2 = b2 - 15. * c3;
      c1 = b1 - 6. * b2 + 45. * c3;
      c0 = 1. - b1 + 3. * b2 - 15. * c3;
      c = 0.1069 / mu; /* guarantees majorization by the 'hat'-function. */
    }

    if (g >= 0.) {
      /* 'Subroutine' F is called (kflag=0 for correct return) */
      kflag = 0;
      //goto Step_F;
      gotoStepF = true;
    }


    for (;;) {
      if (!gotoStepF) {
        /* Step E. Exponential Sample */

        E = Exponantial.exp_rand(context);	/* ~ Exp(1) (standard exponential) */

        /*  sample t from the laplace 'hat'
        (if t <= -0.6744 then pk < fk for all mu >= 10.) */
        u = 2 * context.rng.unif_rand() - 1.;
        t = 1.8 + fsign(E, u);
        if (t > -0.6744) {
          pois = Math.floor(mu + s * t);
          fk = pois;
          difmuk = mu - fk;

          /* 'subroutine' F is called (kflag=1 for correct return) */
          kflag = 1;
        } //End of gotoStepF hack!



        //Step_F: 'subroutine' F : calculation of px,py,fx,fy.

        if (pois < 10) { /* use factorials from table fact[] */
          px = -mu;
          py = Math.pow(mu, pois) / fact[(int) pois];
        } else {
          /* Case pois >= 10 uses polynomial approximation
          a0-a7 for accuracy when advisable */
          del = one_12 / fk;
          del = del * (1. - 4.8 * del * del);
          v = difmuk / fk;
          if (Math.abs(v) <= 0.25) {
            px = fk * v * v * (((((((a7 * v + a6) * v + a5) * v + a4)
                    * v + a3) * v + a2) * v + a1) * v + a0)
                    - del;
          } else /* |v| > 1/4 */ {
            px = fk * Math.log(1. + v) - difmuk - del;
          }
          py = M_1_SQRT_2PI / Math.sqrt(fk);
        }
        x = (0.5 - difmuk) / s;
        x *= x;/* x^2 */
        fx = -0.5 * x;
        fy = omega * (((c3 * x + c2) * x + c1) * x + c0);
        if (kflag > 0) {
          /* Step H. Hat acceptance (E is repeated on rejection) */
          if (c * Math.abs(u) <= py * Math.exp(px + E) - fy * Math.exp(fx + E)) {
            break;
          }
        } else /* Step Q. Quotient acceptance (rare case) */ if (fy - u * fy <= py * Math.exp(px - fx)) {
          break;
        }
      }/* t > -.67.. */
    }
    return pois;
  }

  public static double dpois_raw(double x, double lambda, boolean give_log) {
    /*       x >= 0 ; integer for dpois(), but not e.g. for pgamma()!
    lambda >= 0
     */
    if (lambda == 0) {
      return ((x == 0) ? SignRank.R_D__1(true, give_log) : SignRank.R_D__0(true, give_log));
    }
    if (!DoubleVector.isFinite(lambda)) {
      return SignRank.R_D__0(true, give_log);
    }
    if (x < 0) {
      return (SignRank.R_D__0(true, give_log));
    }
    if (x <= lambda * Double.MIN_VALUE) {
      return (SignRank.R_D_exp(-lambda, true, give_log));
    }
    if (lambda < x * Double.MIN_VALUE) {
      return (SignRank.R_D_exp(-lambda + x * Math.log(lambda) - Gamma.logGamma(x + 1), true, give_log));
    }
    return (SignRank.R_D_fexp(2 * Math.PI * x, -Binom.stirlerr(x) - Binom.bd0(x, lambda), true, give_log));
  }
}
