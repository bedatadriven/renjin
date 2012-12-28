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


public class Normal {

  final static double[] a = new double[]{
    0.0000000, 0.03917609, 0.07841241, 0.1177699,
    0.1573107, 0.19709910, 0.23720210, 0.2776904,
    0.3186394, 0.36012990, 0.40225010, 0.4450965,
    0.4887764, 0.53340970, 0.57913220, 0.6260990,
    0.6744898, 0.72451440, 0.77642180, 0.8305109,
    0.8871466, 0.94678180, 1.00999000, 1.0775160,
    1.1503490, 1.22985900, 1.31801100, 1.4177970,
    1.5341210, 1.67594000, 1.86273200, 2.1538750
  };
  final static double[] d = new double[]{
    0.0000000, 0.0000000, 0.0000000, 0.0000000,
    0.0000000, 0.2636843, 0.2425085, 0.2255674,
    0.2116342, 0.1999243, 0.1899108, 0.1812252,
    0.1736014, 0.1668419, 0.1607967, 0.1553497,
    0.1504094, 0.1459026, 0.1417700, 0.1379632,
    0.1344418, 0.1311722, 0.1281260, 0.1252791,
    0.1226109, 0.1201036, 0.1177417, 0.1155119,
    0.1134023, 0.1114027, 0.1095039
  };
  final static double[] t = new double[]{
    7.673828e-4, 0.002306870, 0.003860618, 0.005438454,
    0.007050699, 0.008708396, 0.010423570, 0.012209530,
    0.014081250, 0.016055790, 0.018152900, 0.020395730,
    0.022811770, 0.025434070, 0.028302960, 0.031468220,
    0.034992330, 0.038954830, 0.043458780, 0.048640350,
    0.054683340, 0.061842220, 0.070479830, 0.081131950,
    0.094624440, 0.112300100, 0.136498000, 0.171688600,
    0.227624100, 0.330498000, 0.584703100
  };
  final static double[] h = new double[]{
    0.03920617, 0.03932705, 0.03950999, 0.03975703,
    0.04007093, 0.04045533, 0.04091481, 0.04145507,
    0.04208311, 0.04280748, 0.04363863, 0.04458932,
    0.04567523, 0.04691571, 0.04833487, 0.04996298,
    0.05183859, 0.05401138, 0.05654656, 0.05953130,
    0.06308489, 0.06737503, 0.07264544, 0.07926471,
    0.08781922, 0.09930398, 0.11555990, 0.14043440,
    0.18361420, 0.27900160, 0.70104740
  };
  final static double A = 2.216035867166471;
  static double C1 = 0.398942280401433;
  static double C2 = 0.180025191068563;

  static double g(double x) {
    return (C1 * Math.exp(-x * x / 2.0) - C2 * (A - x));
  }
  /*
   * This is hard coded in snorm.c
   * so i will not implement other methods but just inversion.
   */
  static N01type N01_kind = N01type.INVERSION;

  
  public static double rnorm(Session context, double mu, double sigma) {
    if ((Double.NaN == mu) || sigma < 0.) {
      return (Double.NaN);
    }
    if (sigma == 0.) {
      return mu; 
    } else {
      return mu + sigma * norm_rand(context);
    }
  }

  
  public static double norm_rand(Session context) {
    double s, u1, w, y, u2, u3, aa, tt, theta, R;
    int i;

    switch (N01_kind) {

      case AHRENS_DIETER: /* see Reference above */

        u1 = context.rng.unif_rand();
        s = 0.0;
        if (u1 > 0.5) {
          s = 1.0;
        }
        u1 = u1 + u1 - s;
        u1 *= 32.0;
        i = (int) u1;
        if (i == 32) {
          i = 31;
        }
        if (i != 0) {
          u2 = u1 - i;
          aa = a[i - 1];
          while (u2 <= t[i - 1]) {
            u1 = context.rng.unif_rand();
            w = u1 * (a[i] - aa);
            tt = (w * 0.5 + aa) * w;
            for (;;) {
              if (u2 > tt) {
                y = aa + w;
                return (s == 1.0) ? -y : y;
              }

              u1 = context.rng.unif_rand();
              if (u2 < u1) {
                break;
              }
              tt = u1;
              u2 = context.rng.unif_rand();
            }
            u2 = context.rng.unif_rand();
          }
          w = (u2 - t[i - 1]) * h[i - 1];
        } else {
          i = 6;
          aa = a[31];
          for (;;) {
            u1 = u1 + u1;
            if (u1 >= 1.0) {
              break;
            }
            aa = aa + d[i - 1];
            i = i + 1;
          }
          u1 = u1 - 1.0;
          for (;;) {
            w = u1 * d[i - 1];
            tt = (w * 0.5 + aa) * w;
            for (;;) {
              u2 = context.rng.unif_rand();
              if (u2 > tt) {
                y = aa + w;
                return (s == 1.0) ? -y : y;
              }
              u1 = context.rng.unif_rand();
              if (u2 < u1) {
                break;
              }
              tt = u1;
            }
            u1 = context.rng.unif_rand();
          }

        }

        y = aa + w;
        return (s == 1.0) ? -y : y;

      /*-----------------------------------------------------------*/

      case BUGGY_KINDERMAN_RAMAGE: /* see Reference above */
        /* note: this has problems, but is retained for
         * reproducibility of older codes, with the same
         * numeric code */
        u1 = context.rng.unif_rand();
        if (u1 < 0.884070402298758) {
          u2 = context.rng.unif_rand();
          return A * (1.13113163544180 * u1 + u2 - 1);
        }

        if (u1 >= 0.973310954173898) { /* tail: */
          for (;;) {
            u2 = context.rng.unif_rand();
            u3 = context.rng.unif_rand();
            tt = (A * A - 2 * Math.log(u3));
            if (u2 * u2 < (A * A) / tt) {
              return (u1 < 0.986655477086949) ? Math.sqrt(tt) : -Math.sqrt(tt);
            }
          }
        }

        if (u1 >= 0.958720824790463) { /* region3: */
          for (;;) {
            u2 = context.rng.unif_rand();
            u3 = context.rng.unif_rand();
            tt = A - 0.630834801921960 * Math.min(u2, u3);
            if (Math.max(u2, u3) <= 0.755591531667601) {
              return (u2 < u3) ? tt : -tt;
            }
            if (0.034240503750111 * Math.abs(u2 - u3) <= g(tt)) {
              return (u2 < u3) ? tt : -tt;
            }
          }
        }

        if (u1 >= 0.911312780288703) { /* region2: */
          for (;;) {
            u2 = context.rng.unif_rand();
            u3 = context.rng.unif_rand();
            tt = 0.479727404222441 + 1.105473661022070 * Math.min(u2, u3);
            if (Math.max(u2, u3) <= 0.872834976671790) {
              return (u2 < u3) ? tt : -tt;
            }
            if (0.049264496373128 * Math.abs(u2 - u3) <= g(tt)) {
              return (u2 < u3) ? tt : -tt;
            }
          }
        }

        /* ELSE	 region1: */
        for (;;) {
          u2 = context.rng.unif_rand();
          u3 = context.rng.unif_rand();
          tt = 0.479727404222441 - 0.595507138015940 * Math.min(u2, u3);
          if (Math.max(u2, u3) <= 0.805577924423817) {
            return (u2 < u3) ? tt : -tt;
          }
        }
      case BOX_MULLER:
        throw new UnsupportedOperationException(N01_kind.toString() + " not implemented yet");

      case USER_NORM:
        throw new UnsupportedOperationException(N01_kind.toString() + " not implemented yet");

      case INVERSION:
        int BIG = 134217728; /* 2^27 */
        /* unif_rand() alone is not of high enough precision */
        u1 = context.rng.unif_rand();
        u1 = (int) (BIG * u1) + context.rng.unif_rand();
        return qnorm5(u1 / BIG, 0.0, 1.0, 1, 0);
      case KINDERMAN_RAMAGE: /* see Reference above */
        /* corrected version from Josef Leydold
         * */
        u1 = context.rng.unif_rand();
        if (u1 < 0.884070402298758) {
          u2 = context.rng.unif_rand();
          return A * (1.131131635444180 * u1 + u2 - 1);
        }

        if (u1 >= 0.973310954173898) { /* tail: */
          for (;;) {
            u2 = context.rng.unif_rand();
            u3 = context.rng.unif_rand();
            tt = (A * A - 2 * Math.log(u3));
            if (u2 * u2 < (A * A) / tt) {
              return (u1 < 0.986655477086949) ? Math.sqrt(tt) : -Math.sqrt(tt);
            }
          }
        }

        if (u1 >= 0.958720824790463) { /* region3: */
          for (;;) {
            u2 = context.rng.unif_rand();
            u3 = context.rng.unif_rand();
            tt = A - 0.630834801921960 * Math.min(u2, u3);
            if (Math.max(u2, u3) <= 0.755591531667601) {
              return (u2 < u3) ? tt : -tt;
            }
            if (0.034240503750111 * Math.abs(u2 - u3) <= g(tt)) {
              return (u2 < u3) ? tt : -tt;
            }
          }
        }

        if (u1 >= 0.911312780288703) { /* region2: */
          for (;;) {
            u2 = context.rng.unif_rand();
            u3 = context.rng.unif_rand();
            tt = 0.479727404222441 + 1.105473661022070 * Math.min(u2, u3);
            if (Math.max(u2, u3) <= 0.872834976671790) {
              return (u2 < u3) ? tt : -tt;
            }
            if (0.049264496373128 * Math.abs(u2 - u3) <= g(tt)) {
              return (u2 < u3) ? tt : -tt;
            }
          }
        }

        /* ELSE	 region1: */
        for (;;) {
          u2 = context.rng.unif_rand();
          u3 = context.rng.unif_rand();
          tt = 0.479727404222441 - 0.595507138015940 * Math.min(u2, u3);
          if (tt < 0.) {
            continue;
          }
          if (Math.max(u2, u3) <= 0.805577924423817) {
            return (u2 < u3) ? tt : -tt;
          }
          if (0.053377549506886 * Math.abs(u2 - u3) <= g(tt)) {
            return (u2 < u3) ? tt : -tt;
          }
        }
      default:
        throw new UnsupportedOperationException("Unsupported type: " + N01_kind);
    }/*switch*/
  }

  static double R_D_val(double x, double log_p) {
    if (log_p != 0) {
      return (Math.log(x));
    } else {
      return (x);
    }
  }

  static double R_DT_qIv(double p, double log_p, double lower_tail) {
    return (R_D_Lval(R_D_qIv(p, log_p), lower_tail));
  }

  static double R_D_Lval(double p, double lower_tail) {
    if (lower_tail != 0) {
      return (p);
    } else {
      return (0.5 - (p) + 0.5);
    }
  }

  static double R_D_qIv(double p, double log_p) {
    if (log_p != 0) {
      return Math.exp(p);
    } else {
      return (p);
    }
  }

  static double R_DT_CIv(double p, double log_p, double lower_tail) {
    return (R_D_Cval(R_D_qIv(p, log_p), lower_tail));
  }

  static double R_D_Cval(double p, double lower_tail) {
    if (lower_tail != 0) {
      return (0.5 - (p) + 0.5);
    } else {
      return (p);
    }
  }

  static double qnorm5(double p, double mu, double sigma, int lower_tail, int log_p) {
    double p_, q, r, val;

    if ((p == Double.NaN) || (mu == Double.NaN) || (sigma == Double.NaN)) {
      return p + mu + sigma;
    }

    /* 
     * 0<= P <= 1 control. Do it later.
     * R_Q_P01_boundaries(p, ML_NEGINF, ML_POSINF);
     */

    if (sigma < 0) {
      return (Double.NaN);
    };

    if (sigma == 0) {
      return mu;
    }

    /*
     *  p_ = R_DT_qIv(p);/* real lower_tail prob. p 
     */
    p_ = R_DT_qIv(p, log_p, lower_tail);
    q = p_ - 0.5;


    /* double ppnd16_(double *p, long *ifault)*/ /*      ALGORITHM AS241  APPL. STATIST. (1988) VOL. 37, NO. 3
    
    Produces the normal deviate Z corresponding to a given lower
    tail area of P; Z is accurate to about 1 part in 10**16.
    
    (original fortran code used PARAMETER(..) for the coefficients
    and provided hash codes for checking them...)
     */
    if (Math.abs(q) <= .425) {/* 0.075 <= p <= 0.925 */
      r = .180625 - q * q;
      val =
              q * (((((((r * 2509.0809287301226727
              + 33430.575583588128105) * r + 67265.770927008700853) * r
              + 45921.953931549871457) * r + 13731.693765509461125) * r
              + 1971.5909503065514427) * r + 133.14166789178437745) * r
              + 3.387132872796366608)
              / (((((((r * 5226.495278852854561
              + 28729.085735721942674) * r + 39307.89580009271061) * r
              + 21213.794301586595867) * r + 5394.1960214247511077) * r
              + 687.1870074920579083) * r + 42.313330701600911252) * r + 1.);
    } else { /* closer than 0.075 from {0,1} boundary */

      /* r = min(p, 1-p) < 0.075 */
      if (q > 0) {
        r = R_DT_CIv(p, log_p, lower_tail);/* 1-p */
      } else {
        r = p_;/* = R_DT_Iv(p) ^=  p */
      }

      /*
       * r = sqrt(- ((log_p && ((lower_tail && q <= 0) || (!lower_tail && q > 0))) ?
      p :  log(r)));
       */
      if (((log_p != 0 && (((lower_tail != 0) && q <= 0) || (lower_tail == 0 && q > 0))))) {
        r = Math.sqrt(-p);
      } else {
        r = Math.sqrt(-Math.log(r));
      }

      /* r = sqrt(-log(r))  <==>  min(p, 1-p) = exp( - r^2 ) */


      if (r <= 5.) { /* <==> min(p,1-p) >= exp(-25) ~= 1.3888e-11 */
        r += -1.6;
        val = (((((((r * 7.7454501427834140764e-4
                + .0227238449892691845833) * r + .24178072517745061177)
                * r + 1.27045825245236838258) * r
                + 3.64784832476320460504) * r + 5.7694972214606914055)
                * r + 4.6303378461565452959) * r
                + 1.42343711074968357734)
                / (((((((r
                * 1.05075007164441684324e-9 + 5.475938084995344946e-4)
                * r + .0151986665636164571966) * r
                + .14810397642748007459) * r + .68976733498510000455)
                * r + 1.6763848301838038494) * r
                + 2.05319162663775882187) * r + 1.);
      } else { /* very close to  0 or 1 */
        r += -5.;
        val = (((((((r * 2.01033439929228813265e-7
                + 2.71155556874348757815e-5) * r
                + .0012426609473880784386) * r + .026532189526576123093)
                * r + .29656057182850489123) * r
                + 1.7848265399172913358) * r + 5.4637849111641143699)
                * r + 6.6579046435011037772)
                / (((((((r
                * 2.04426310338993978564e-15 + 1.4215117583164458887e-7)
                * r + 1.8463183175100546818e-5) * r
                + 7.868691311456132591e-4) * r + .0148753612908506148525)
                * r + .13692988092273580531) * r
                + .59983220655588793769) * r + 1.);
      }

      if (q < 0.0) {
        val = -val;
      }
      /* return (q >= 0.)? r : -r ;*/
    }
    return mu + sigma * val;
  }
}
