package org.renjin.primitives.random.distributions;

public class DistributionUtilities {

  public static double ML_NEGINF = Double.NEGATIVE_INFINITY;
  public static double DBL_EPSILON = Math.pow (2.0 , -52);

  public static double R_DT_0(boolean lower_tail, boolean log_p) {
    if (lower_tail) {
      return R_D__0(lower_tail, log_p);
    } else {
      return R_D__1(lower_tail, log_p);
    }
  }

  public static double R_D__1(boolean lower_tail, boolean log_p) {
    if (log_p) {
      return 0.;
    } else {
      return 1.;
    }
  }

  public static double R_D__0(boolean lower_tail, boolean log_p) {
    if (log_p) {
      return ML_NEGINF;
    } else {
      return 0.0;
    }
  }

  public static double R_DT_1(boolean lower_tail, boolean log_p) {
    if (lower_tail) {
      return R_D__1(lower_tail, log_p);
    } else {
      return R_D__0(lower_tail, log_p);
    }
  }

  public static double R_P_bounds_01(double x, double x_min, double x_max, boolean lower_tail, boolean log_p) {
    if (x <= x_min) {
      return R_DT_0(lower_tail, log_p);
    }
    if (x >= x_max) {
      return R_DT_1(lower_tail, log_p);
    }
    return (Double.NaN);
  }
}
