package org.renjin.primitives.random;


import org.renjin.eval.Session;
import org.renjin.sexp.DoubleVector;


public class F {

  public static double rf(Session context, double n1, double n2) {
    double v1, v2;
    if (Double.isNaN(n1) || Double.isNaN(n2) || n1 <= 0. || n2 <= 0.) {
      return (Double.NaN);
    }


    //v1 = R_FINITE(n1) ? (rchisq(n1) / n1) : 1;
    //v2 = R_FINITE(n2) ? (rchisq(n2) / n2) : 1;

    if (Double.isInfinite(n1)) {
      v1 = ChiSquare.rchisq(context, n1) / n1;
    } else {
      v1 = 1;
    }

    if (Double.isInfinite(n2)) {
      v2 = ChiSquare.rchisq(context, n2) / n2;
    } else {
      v2 = 1;
    }

    return v1 / v2;
  }

  public static double dnf(double x, double df1, double df2, double ncp, boolean give_log) {
    double y, z, f;

    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(df1) || DoubleVector.isNaN(df2) || DoubleVector.isNaN(ncp)) {
      return x + df2 + df1 + ncp;
    }

    /* want to compare dnf(ncp=0) behavior with df() one, hence *NOT* :
     * if (ncp == 0)
     *   return df(x, df1, df2, give_log); */

    if (df1 <= 0. || df2 <= 0. || ncp < 0) {
      return DoubleVector.NaN;
    }
    if (x < 0.) {
      return (SignRank.R_D__0(true, give_log));
    }
    if (!DoubleVector.isFinite(ncp)) { /* ncp = +Inf -- FIXME?: in some cases, limit exists */
      return DoubleVector.NaN;
    }

    /* This is not correct for  df1 == 2, ncp > 0 - and seems unneeded:
     *  if (x == 0.) return(df1 > 2 ? R_D__0 : (df1 == 2 ? R_D__1 : ML_POSINF));
     */
    if (!DoubleVector.isFinite(df1) && !DoubleVector.isFinite(df2)) { /* both +Inf */
      /* PR: not sure about this (taken from  ncp==0)  -- FIXME ? */
      if (x == 1.) {
        return Double.POSITIVE_INFINITY;
      }
      /* else */ return SignRank.R_D__0(true, give_log);
    }
    if (!DoubleVector.isFinite(df2)) /* i.e.  = +Inf */ {
      return df1 * Distributions.dnchisq(x * df1, df1, ncp, give_log);
    }
    /*	 ==  dngamma(x, df1/2, 2./df1, ncp, give_log)  -- but that does not exist */
    if (df1 > 1e14 && ncp < 1e7) {
      /* includes df1 == +Inf: code below is inaccurate there */
      f = 1 + ncp / df1; /* assumes  ncp << df1 [ignores 2*ncp^(1/2)/df1*x term] */
      z = Distributions.dgamma(1. / x / f, df2 / 2, 2. / df2, give_log);
      return give_log ? z - 2 * Math.log(x) - Math.log(f) : z / (x * x) / f;
    }

    y = (df1 / df2) * x;
    z = Distributions.dnbeta(y / (1 + y), df1 / 2., df2 / 2., ncp, give_log);
    return give_log
            ? z + Math.log(df1) - Math.log(df2) - 2 * Math.log1p(y)
            : z * (df1 / df2) / (1 + y) / (1 + y);
  }

  public static double pnf(double x, double df1, double df2, double ncp, boolean lower_tail, boolean log_p) {
    double y;
    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(df1) || DoubleVector.isNaN(df2) || DoubleVector.isNaN(ncp)) {
      return x + df2 + df1 + ncp;
    }
    if (df1 <= 0. || df2 <= 0. || ncp < 0) {
      return DoubleVector.NaN;
    }
    if (!DoubleVector.isFinite(ncp)) {
      return DoubleVector.NaN;
    }
    if (!DoubleVector.isFinite(df1) && !DoubleVector.isFinite(df2)) { /* both +Inf */
      return DoubleVector.NaN;
    }

    //R_P_bounds_01(x, 0., ML_POSINF);
    if (x <= 0.0) {
      return SignRank.R_DT_0(lower_tail, log_p);
    }
    if (x >= Double.POSITIVE_INFINITY) {
      return SignRank.R_DT_1(lower_tail, log_p);
    }

    if (df2 > 1e8) /* avoid problems with +Inf and loss of accuracy */ {
      return Distributions.pnchisq(x * df1, df1, ncp, lower_tail, log_p);
    }

    y = (df1 / df2) * x;
    return Beta.pnbeta2(y / (1. + y), 1. / (1. + y), df1 / 2., df2 / 2.,
            ncp, lower_tail, log_p);
  }

  public static double qnf(double p, double df1, double df2, double ncp, boolean lower_tail, boolean log_p) {
    double y;

    if (DoubleVector.isNaN(p) || DoubleVector.isNaN(df1) || DoubleVector.isNaN(df2) || DoubleVector.isNaN(ncp)) {
      return p + df1 + df2 + ncp;
    }

    if (df1 <= 0. || df2 <= 0. || ncp < 0) {
      return DoubleVector.NaN;
    }
    if (!DoubleVector.isFinite(ncp)) {
      return DoubleVector.NaN;
    }
    if (!DoubleVector.isFinite(df1) && !DoubleVector.isFinite(df2)) {
      return DoubleVector.NaN;
    }

    //R_Q_P01_boundaries(p, 0, ML_POSINF);
    if ((log_p && p > 0) || (!log_p && (p < 0 || p > 1))) {
      return DoubleVector.NaN;
    }
    if (p == SignRank.R_DT_0(lower_tail, log_p)) {
      return 0;
    }
    if (p == SignRank.R_DT_1(lower_tail, log_p)) {
      return Double.POSITIVE_INFINITY;
    }
    //end of R_Q_P01_boundaries

    if (df2 > 1e8) /* avoid problems with +Inf and loss of accuracy */ {
      return Distributions.qnchisq(p, df1, ncp, lower_tail, log_p) / df1;
    }

    y = Beta.qnbeta(p, df1 / 2., df2 / 2., ncp, lower_tail, log_p);
    return y / (1 - y) * (df2 / df1);
  }
}
