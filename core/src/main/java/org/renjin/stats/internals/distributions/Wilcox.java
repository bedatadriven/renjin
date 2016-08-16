package org.renjin.stats.internals.distributions;

import org.renjin.eval.Session;


public class Wilcox {

  public static double rwilcox(Session context, double m, double n) {
    int i, j, k;
    int[] x;
    double r;


    /* NaNs propagated correctly */
    if (Double.isNaN(m) || Double.isNaN(n)) {
      return (m + n);
    }

    m = Math.floor(m + 0.5);
    n = Math.floor(n + 0.5);
    if ((m < 0) || (n < 0)) {
      return Double.NaN;
    }

    if ((m == 0) || (n == 0)) {
      return (0);
    }

    r = 0.0;
    k = (int) (m + n);
    x = new int[k];
    for (i = 0; i < k; i++) {
      x[i] = i;
    }

    for (i = 0; i < n; i++) {
      j = (int) (Math.floor(k * context.rng.unif_rand()));
      r += x[j];
      x[j] = x[--k];
    }
    return (r - n * (n - 1) / 2);
  }


}
