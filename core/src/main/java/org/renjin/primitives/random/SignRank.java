package org.renjin.primitives.random;

import org.renjin.eval.Session;

public class SignRank {

  public static double DBL_EPSILON = Math.pow(2., -52.);
  private static double[] w;
  private static int allocated_n;

  public static double R_D__0(boolean lower_tail, boolean log_p) {
    return (log_p ? Double.NEGATIVE_INFINITY : 0.);
  }

  public static double R_D__1(boolean lower_tail, boolean log_p) {
    return (log_p ? 0. : 1.);
  }

  public static double R_DT_0(boolean lower_tail, boolean log_p) {
    return (lower_tail ? R_D__0(lower_tail, log_p) : R_D__1(lower_tail, log_p));
  }

  public static double R_DT_1(boolean lower_tail, boolean log_p) {
    return (lower_tail ? R_D__1(lower_tail, log_p) : R_D__0(lower_tail, log_p));
  }

  public static double R_DT_val(double x, boolean lower_tail, boolean log_p) {
    return (lower_tail ? R_D_val(x, lower_tail, log_p) : R_D_Clog(x, lower_tail, log_p));
  }

  public static double R_D_val(double x, boolean lower_tail, boolean log_p) {
    return (log_p ? Math.log(x) : (x));
  }

  public static double R_D_Clog(double p, boolean lower_tail, boolean log_p) {
    return (log_p ? Math.log1p(-(p)) : (0.5 - (p) + 0.5));
  }

  public static double R_D_exp(double x, boolean lower_tail, boolean log_p) {
    return (log_p ? (x) : Math.exp(x));
  }

  public static double R_DT_Clog(double p, boolean lower_tail, boolean log_p) {
    return (lower_tail ? R_D_LExp(p, lower_tail, log_p) : R_D_log(p, lower_tail, log_p));
  }

  public static double R_D_log(double p, boolean lower_tail, boolean log_p) {
    return (log_p ? (p) : Math.log(p));
  }

  public static double R_D_LExp(double x, boolean lower_tail, boolean log_p) {
    return (log_p ? R_Log1_Exp(x, lower_tail, log_p) : Math.log1p(-x));
  }

  public static double R_Log1_Exp(double x, boolean lower_tail, boolean log_p) {
    return ((x) > -Math.log(2.0) ? Math.log(-Math.expm1(x)) : Math.log1p(-Math.exp(x)));
  }

  public static boolean R_D_nonint(double x, boolean lower_tail, boolean log_p) {
    return (Math.abs((x) - Math.floor((x) + 0.5)) > 1e-7);
  }

  public static double R_D_forceint(double x) {
    return Math.floor((x) + 0.5);
  }

  public static double R_D_fexp(double f, double x, boolean lower_tail, boolean log_p) {
    return (log_p ? -0.5 * Math.log(f) + (x) : Math.exp(x) / Math.sqrt(f));
  }
  

  /*
   * Random Number Generator for SignRank
   * 
   */
  public static double rsignrank(Session context, double n) {
    int i, k;
    double r;


    /* NaNs propagated correctly */
    if (Double.isNaN(n)) {
      return (n);
    }

    n = Math.floor(n + 0.5);
    if (n < 0) {
      return Double.NaN;
    }

    if (n == 0) {
      return (0);
    }

    r = 0.0;
    k = (int) n;
    for (i = 0; i < k;) {
      r += (++i) * Math.floor(context.rng.unif_rand() + 0.5);
    }
    return (r);

  }
  /*
   * End of random number generator of SignRank
   */

  private static void w_init_maybe(int n) {
    int u, c;

    u = n * (n + 1) / 2;
    c = (u / 2);

    w = new double[c + 1];
    allocated_n = w.length;
  }

  public static double csignrank(int k, int n) {
    int c, u, j;

    u = n * (n + 1) / 2;
    c = (u / 2);

    if (k < 0 || k > u) {
      return 0;
    }
    if (k > c) {
      k = u - k;
    }

    if (n == 1) {
      return 1.;
    }
    if (w[0] == 1.) {
      return w[k];
    }

    w[0] = w[1] = 1.;
    for (j = 2; j < n + 1; ++j) {
      int i, end = Math.min(j * (j + 1) / 2, c);
      for (i = end; i >= j; --i) {
        w[i] += w[i - j];
      }
    }

    return w[k];
  }

  public static double qsignrank(double x, double n, boolean lower_tail, boolean log_p) {
    double f, p, q;


    if (Double.isNaN(x) || Double.isNaN(n)) {
      return (x + n);
    }

    if (Double.isInfinite(x) || Double.isInfinite(n)) {
      return Double.NaN;
    }

    if ((log_p && x > 0) || (!log_p && (x < 0 || x > 1))) {
      return Double.NaN;
    }

    n = Math.floor(n + 0.5);
    if (n <= 0) {
      return Double.NaN;
    }

    if (x == R_DT_0(lower_tail, log_p)) {
      return (0);
    }

    if (x == R_DT_1(lower_tail, log_p)) {
      return (n * (n + 1) / 2);
    }

    if (log_p || !lower_tail) {
      //x = R_DT_qIv(x); /* lower_tail,non-log "p" */
      x = Normal.R_DT_qIv(x, log_p ? 1. : 0., lower_tail ? 1. : 0.);
    }

    w_init_maybe((int) n);
    f = Math.exp(-n * Math.log(2.));

    p = 0;
    q = 0;
    if (x <= 0.5) {
      x = x - 10 * DBL_EPSILON;
      for (;;) {
        p += csignrank((int) q, (int) n) * f;
        if (p >= x) {
          break;
        }
        q++;
      }
    } else {
      x = 1 - x + 10 * DBL_EPSILON;
      for (;;) {
        p += csignrank((int) q, (int) n) * f;
        if (p > x) {
          q = n * (n + 1) / 2 - q;
          break;
        }
        q++;
      }
    }

    return (q);
  }

  public static double psignrank(double x, double n, boolean lower_tail, boolean log_p) {
    int i;
    double f, p;

    if (Double.isNaN(x) || Double.isNaN(n)) {
      return (x + n);
    }

    if (Double.isInfinite(n)) {
      return Double.NaN;
    }

    n = Math.floor(n + 0.5);
    if (n <= 0) {
      return Double.NaN;
    }

    x = Math.floor(x + 1e-7);
    if (x < 0.0) {
      return (R_DT_0(lower_tail, log_p));
    }

    if (x >= n * (n + 1) / 2) {
      return (R_DT_1(lower_tail, log_p));
    }

    w_init_maybe((int) n);
    f = Math.exp(-n * Math.log(2.));
    p = 0;
    if (x <= (n * (n + 1) / 4)) {
      for (i = 0; i <= x; i++) {
        p += csignrank(i, (int) n) * f;
      }
    } else {
      x = n * (n + 1) / 2 - x;
      for (i = 0; i < x; i++) {
        p += csignrank(i, (int) n) * f;
      }
      lower_tail = !lower_tail; /* p = 1 - p; */
    }

    return (R_DT_val(p, lower_tail, log_p));
  } /* psignrank() */


  public static double dsignrank(double x, double n, boolean give_log) {
    double d;


    /* NaNs propagated correctly */
    if (Double.isNaN(x) || Double.isNaN(n)) {
      return (x + n);
    }

    n = Math.floor(n + 0.5);
    if (n <= 0) {
      return Double.NaN;
    }

    if (Math.abs(x - Math.floor(x + 0.5)) > 1e-7) {
      return (R_D__0(true, give_log));
    }

    x = Math.floor(x + 0.5);
    if ((x < 0) || (x > (n * (n + 1) / 2))) {
      return (R_D__0(true, give_log));
    }

    w_init_maybe((int) n);
    d = R_D_exp(Math.log(csignrank((int) x, (int) n)) - n * Math.log(2.), true, give_log);

    return (d);
  }
}
