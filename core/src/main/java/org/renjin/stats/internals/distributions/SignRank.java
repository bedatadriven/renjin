package org.renjin.stats.internals.distributions;

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


}
