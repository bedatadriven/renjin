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
import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Internal;

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
  @Internal
  @DataParallel
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

}