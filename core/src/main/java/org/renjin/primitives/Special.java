package org.renjin.primitives;

import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Deferrable;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.PreserveAttributeStyle;

/**
 * Special mathematical functions related to the beta and gamma
 * functions.
 */
public class Special {

  @Internal
  @Deferrable
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double beta(double a, double b) {
    return org.renjin.nmath.beta.beta(a, b);
  }

  @Internal
  @Deferrable
  @DataParallel
  public static double lbeta(double a, double b) {
    return org.renjin.nmath.lbeta.lbeta(a, b);
  }

  @Internal
  @DataParallel
  public static double choose(double n, int k) {
    return org.renjin.nmath.choose.choose(n, k);
  }

  @Internal
  @DataParallel
  public static double lchoose(double n, int k) {
    return org.renjin.nmath.choose.lchoose(n, k);
  }


  @Internal
  @DataParallel
  public static double psigamma(double x, double deriv) {
    return org.renjin.nmath.polygamma.psigamma(x, deriv);
  }
  
  @Internal
  @DataParallel
  public static double besselI(double x, double alpha, double expo) {
    return org.renjin.nmath.bessel_i.bessel_i(x, alpha, expo);
  }

  @Internal
  @DataParallel
  public static double besselJ(double x, double alpha) {
    return org.renjin.nmath.bessel_j.bessel_j(x, alpha);
  }

  @Internal
  @DataParallel
  public static double besselK(double x, double alpha, double expo) {
    return org.renjin.nmath.bessel_k(x, alpha, expo);
  }

  @Internal
  @DataParallel
  public static double besselY(double x, double alpha) {
    return org.renjin.nmath.bessel_y.bessel_y(x, alpha);
  }
}
