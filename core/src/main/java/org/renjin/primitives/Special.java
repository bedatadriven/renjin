package org.renjin.primitives;

import org.apache.commons.math.special.Beta;
import org.apache.commons.math.util.MathUtils;
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
    return (Math.exp(Beta.logBeta(a, b)));
  }

  @Internal
  @Deferrable
  @DataParallel
  public static double lbeta(double a, double b) {
    return (Beta.logBeta(a, b));
  }

  @Internal
  @DataParallel
  public static double choose(double n, int k) {
    /*
     * Because gamma(a+1) = factorial(a)
     * we use gamma(n+1) /(gamma(n-k+1) * gamma(k+1)) instead of
     * Binomial(n,k) = n! / ((n-k)! * k!) for non-integer n values.
     * 
     */
    if (k < 0) {
      return (0);
    } else if (k == 0) {
      return (1);
    } else if ((int) n == n) {
      return (MathUtils.binomialCoefficientDouble((int) n, k));
    } else {
      return (MathGroup.gamma(n + 1) / (MathGroup.gamma(n - k + 1) * MathGroup.gamma(k + 1)));
    }
  }

  @Internal
  @DataParallel
  public static double lchoose(double n, int k) {
    return (Math.log(choose(n, k)));
  }

}
