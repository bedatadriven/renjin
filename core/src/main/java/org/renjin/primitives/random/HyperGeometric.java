package org.renjin.primitives.random;

import org.renjin.eval.Session;
import org.renjin.sexp.DoubleVector;


public class HyperGeometric {

  private static double afc(int i) {
    final double[] al = new double[]{
      0.0,
      0.0,/*ln(0!)=ln(1)*/
      0.0,/*ln(1!)=ln(1)*/
      0.69314718055994530941723212145817,/*ln(2) */
      1.79175946922805500081247735838070,/*ln(6) */
      3.17805383034794561964694160129705,/*ln(24)*/
      4.78749174278204599424770093452324,
      6.57925121201010099506017829290394,
      8.52516136106541430016553103634712
    /*, 10.60460290274525022841722740072165*/
    };

    double di, value;

    if (i < 0) {
      //MATHLIB_WARNING(("rhyper.c: afc(i), i=%d < 0 -- SHOULD NOT HAPPEN!\n"),i);
      return -1;/* unreached (Wall) */
    } else if (i <= 7) {
      value = al[i + 1];
    } else {
      di = i;
      value = (di + 0.5) * Math.log(di) - di + 0.08333333333333 / di
              - 0.00277777777777 / di / di / di + 0.9189385332;
    }
    return value;
  }

  public static class Random_hyper_geometric {

    final static double con = 57.56462733;
    final static double deltal = 0.0078;
    final static double deltau = 0.0034;
    final static double scale = 1e25;
    static int nn1, nn2, kk;
    static int i, ix;
    static boolean reject, setup1, setup2;
    static double e, f, g, p, r, t, u, v, y;
    static double de, dg, dr, ds, dt, gl, gu, nk, nm, ub;
    static double xk, xm, xn, y1, ym, yn, yk, alv;
    static int ks = -1;
    static int n1s = -1, n2s = -1;
    static int k, m;
    static int minjx, maxjx, n1, n2;
    static double a, d, s, w;
    static double tn, xl, xr, kl, kr, lamdl, lamdr, p1, p2, p3;

    public static double rhyper(Session context, double nn1in, double nn2in, double kkin) {


      /* These should become `thread_local globals' : */

      /* check parameter validity */

      if (!DoubleVector.isFinite(nn1in) || !DoubleVector.isFinite(nn2in) || !DoubleVector.isFinite(kkin)) {
        return DoubleVector.NaN;
      }

      nn1 = (int) Math.floor(nn1in + 0.5);
      nn2 = (int) Math.floor(nn2in + 0.5);
      kk = (int) Math.floor(kkin + 0.5);

      if (nn1 < 0 || nn2 < 0 || kk < 0 || kk > nn1 + nn2) {
        return DoubleVector.NaN;
      }

      /* if new parameter values, initialize */
      reject = true;
      if (nn1 != n1s || nn2 != n2s) {
        setup1 = true;
        setup2 = true;
      } else if (kk != ks) {
        setup1 = false;
        setup2 = true;
      } else {
        setup1 = false;
        setup2 = false;
      }
      if (setup1) {
        n1s = nn1;
        n2s = nn2;
        tn = nn1 + nn2;
        if (nn1 <= nn2) {
          n1 = nn1;
          n2 = nn2;
        } else {
          n1 = nn2;
          n2 = nn1;
        }
      }
      if (setup2) {
        ks = kk;
        if (kk + kk >= tn) {
          k = (int) (tn - kk);
        } else {
          k = kk;
        }
      }
      if (setup1 || setup2) {
        m = (int) ((k + 1.0) * (n1 + 1.0) / (tn + 2.0));
        minjx = Math.max(0, k - n2);
        maxjx = Math.min(n1, k);
      }
      /* generate random variate --- Three basic cases */

      if (minjx == maxjx) { /* I: degenerate distribution ---------------- */
        ix = maxjx;
        /* return ix;
        No, need to unmangle <TSL>*/
        /* return appropriate variate */

        if (kk + kk >= tn) {
          if (nn1 > nn2) {
            ix = kk - nn2 + ix;
          } else {
            ix = nn1 - ix;
          }
        } else {
          if (nn1 > nn2) {
            ix = kk - ix;
          }
        }
        return ix;

      } else if (m - minjx < 10) { /* II: inverse transformation ---------- */
        if (setup1 || setup2) {
          if (k < n2) {
            w = Math.exp(con + afc(n2) + afc(n1 + n2 - k)
                    - afc(n2 - k) - afc(n1 + n2));
          } else {
            w = Math.exp(con + afc(n1) + afc(k)
                    - afc(k - n2) - afc(n1 + n2));
          }
        }
        L10:
        L10(context);
        L20:
        L20(context);
      } else { /* III : h2pe --------------------------------------------- */

        if (setup1 || setup2) {
          s = Math.sqrt((tn - k) * k * n1 * n2 / (tn - 1) / tn / tn);

          /* remark: d is defined in reference without int. */
          /* the truncation centers the cell boundaries at 0.5 */

          d = (int) (1.5 * s) + .5;
          xl = m - d + .5;
          xr = m + d + .5;
          a = afc(m) + afc(n1 - m) + afc(k - m) + afc(n2 - k + m);
          kl = Math.exp(a - afc((int) (xl)) - afc((int) (n1 - xl))
                  - afc((int) (k - xl))
                  - afc((int) (n2 - k + xl)));
          kr = Math.exp(a - afc((int) (xr - 1))
                  - afc((int) (n1 - xr + 1))
                  - afc((int) (k - xr + 1))
                  - afc((int) (n2 - k + xr - 1)));
          lamdl = -Math.log(xl * (n2 - k + xl) / (n1 - xl + 1) / (k - xl + 1));
          lamdr = -Math.log((n1 - xr + 1) * (k - xr + 1) / xr / (n2 - k + xr));
          p1 = d + d;
          p2 = p1 + kl / lamdl;
          p3 = p2 + kr / lamdr;
        }
        L30:
        L30(context);
        /* return appropriate variate */

        if (kk + kk >= tn) {
          if (nn1 > nn2) {
            ix = kk - nn2 + ix;
          } else {
            ix = nn1 - ix;
          }
        } else {
          if (nn1 > nn2) {
            ix = kk - ix;
          }
        }
        return ix;
      }
      return ix;
    }

    public static void L10(Session context) {
      //L10:
      p = w;
      ix = minjx;
      u = context.rng.unif_rand() * scale;
      L20(context);
      //??????????????*
    }

    public static void L20(Session context) {
      if (u > p) {
        u -= p;
        p *= (n1 - ix) * (k - ix);
        ix++;
        p = p / ix / (n2 - k + ix);
        if (ix > maxjx) {
          L10(context);
        }
        L20(context);
      }
    }

    public static void L30(Session context) {
      u = context.rng.unif_rand() * p3;
      v = context.rng.unif_rand();
      if (u < p1) { /* rectangular region */
        ix = (int) (xl + u);
      } else if (u <= p2) { /* left tail */
        ix = (int) (xl + Math.log(v) / lamdl);
        if (ix < minjx) {
          L30(context);
        }
        v = v * (u - p1) * lamdl;
      } else { /* right tail */
        ix = (int) (xr - Math.log(v) / lamdr);
        if (ix > maxjx) {
          L30(context);
        }
        v = v * (u - p2) * lamdr;
      }

      /* acceptance/rejection test */

      if (m < 100 || ix <= 50) {
        /* explicit evaluation */
        /* The original algorithm (and TOMS 668) have
        f = f * i * (n2 - k + i) / (n1 - i) / (k - i);
        in the (m > ix) case, but the definition of the
        recurrence relation on p134 shows that the +1 is
        needed. */
        f = 1.0;
        if (m < ix) {
          for (i = m + 1; i <= ix; i++) {
            f = f * (n1 - i + 1) * (k - i + 1) / (n2 - k + i) / i;
          }
        } else if (m > ix) {
          for (i = ix + 1; i <= m; i++) {
            f = f * i * (n2 - k + i) / (n1 - i + 1) / (k - i + 1);
          }
        }
        if (v <= f) {
          reject = false;
        }
      } else {
        /* squeeze using upper and lower bounds */
        y = ix;
        y1 = y + 1.0;
        ym = y - m;
        yn = n1 - y + 1.0;
        yk = k - y + 1.0;
        nk = n2 - k + y1;
        r = -ym / y1;
        s = ym / yn;
        t = ym / yk;
        e = -ym / nk;
        g = yn * yk / (y1 * nk) - 1.0;
        dg = 1.0;
        if (g < 0.0) {
          dg = 1.0 + g;
        }
        gu = g * (1.0 + g * (-0.5 + g / 3.0));
        gl = gu - .25 * (g * g * g * g) / dg;
        xm = m + 0.5;
        xn = n1 - m + 0.5;
        xk = k - m + 0.5;
        nm = n2 - k + xm;
        ub = y * gu - m * gl + deltau
                + xm * r * (1. + r * (-0.5 + r / 3.0))
                + xn * s * (1. + s * (-0.5 + s / 3.0))
                + xk * t * (1. + t * (-0.5 + t / 3.0))
                + nm * e * (1. + e * (-0.5 + e / 3.0));
        /* test against upper bound */
        alv = Math.log(v);
        if (alv > ub) {
          reject = true;
        } else {
          /* test against lower bound */
          dr = xm * (r * r * r * r);
          if (r < 0.0) {
            dr /= (1.0 + r);
          }
          ds = xn * (s * s * s * s);
          if (s < 0.0) {
            ds /= (1.0 + s);
          }
          dt = xk * (t * t * t * t);
          if (t < 0.0) {
            dt /= (1.0 + t);
          }
          de = nm * (e * e * e * e);
          if (e < 0.0) {
            de /= (1.0 + e);
          }
          if (alv < ub - 0.25 * (dr + ds + dt + de)
                  + (y + m) * (gl - gu) - deltal) {
            reject = false;
          } else {
            /* * Stirling's formula to machine accuracy
             */
            if (alv <= (a - afc(ix) - afc(n1 - ix)
                    - afc(k - ix) - afc(n2 - k + ix))) {
              reject = false;
            } else {
              reject = true;
            }
          }
        }
      }
      if (reject) {
        L30(context);
      }
    }
  }
}
