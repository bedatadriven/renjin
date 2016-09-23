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
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;


public class Multinomial {

  public static void rmultinom(Session context, int n, double[] prob, int K, int[] rN) /* `Return' vector  rN[1:K] {K := length(prob)}
   *  where rN[j] ~ Bin(n, prob[j]) ,  sum_j rN[j] == n,  sum_j prob[j] == 1,
   */ {
    int k;
    double pp;
    double p_tot = 0.; /* LDOUBLE */
    /* This calculation is sensitive to exact values, so we try to
    ensure that the calculations are as accurate as possible
    so different platforms are more likely to give the same
    result. */


    if (K < 1) {
      //ML_ERROR(ME_DOMAIN, "rmultinom"); 
      return;
    }
    if (n < 0) {
      return;
    }

    if (K == IntVector.NA || K < 1) {
      //ML_ERROR(ME_DOMAIN, "rmultinom"); 
      return;
    }

    if (n == IntVector.NA || n < 0) {
      return;
    }


    /* Note: prob[K] is only used here for checking  sum_k prob[k] = 1 ;
     *       Could make loop one shorter and drop that check !
     */
    for (k = 0; k < K; k++) {
      pp = prob[k];
      if (!DoubleVector.isFinite(pp) || pp < 0. || pp > 1.) {
        //ML_ERR_ret_NAN(k);
        return;
      }
      p_tot += pp;
      rN[k] = 0;
    }

    if (Math.abs(p_tot - 1.) > 1e-7) //MATHLIB_ERROR(_("rbinom: probability sum should be 1, but is %g"), (double) p_tot);
    {
      if (n == 0) {
        return;
      }
    }

    if (K == 1 && p_tot == 0.) {
      return; /* trivial border case: do as rbinom */
    }

    /* Generate the first K-1 obs. via binomials */

    for (k = 0; k < K - 1; k++) { /* (p_tot, n) are for "remaining binomial" */
      if (prob[k] != 0.0) {
        pp = prob[k] / p_tot;
        /* printf("[%d] %.17f\n", k+1, pp); */
        rN[k] = ((pp < 1.) ? (int) Binom.rbinom(context, (double) n, pp)
                : /*>= 1; > 1 happens because of rounding */ n);
        n -= rN[k];
      } else {
        rN[k] = 0;
      }
      if (n <= 0) /* we have all*/ {
        return;
      }
      p_tot -= prob[k]; /* i.e. = sum(prob[(k+1):K]) */
    }
    rN[K - 1] = n;
    return;
  }
}
