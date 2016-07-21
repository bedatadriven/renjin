package org.renjin.stats.internals.distributions;


import org.renjin.eval.Session;


public class F {

  public static double rf(Session context, double n1, double n2) {
    double v1, v2;
    if (Double.isNaN(n1) || Double.isNaN(n2) || n1 <= 0. || n2 <= 0.) {
      return (Double.NaN);
    }


    //v1 = R_FINITE(n1) ? (rchisq(n1) / n1) : 1;
    //v2 = R_FINITE(n2) ? (rchisq(n2) / n2) : 1;

    if (Double.isInfinite(n1)) {
      v1 = 1;
    } else {
      v1 = ChiSquare.rchisq(context, n1) / n1;
    }

    if (Double.isInfinite(n2)) {
      v2 = 1;
    } else {
      v2 = ChiSquare.rchisq(context, n2) / n2;
    }

    return v1 / v2;
  }

}
