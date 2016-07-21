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
}
