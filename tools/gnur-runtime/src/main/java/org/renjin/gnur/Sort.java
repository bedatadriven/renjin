package org.renjin.gnur;


import org.renjin.gcc.runtime.DoublePtr;

import java.util.Arrays;

/**
 * Internal Sort routines exported to packages
 */
public class Sort {

  /**
   * Partial sort so that x[k] is in the correct place, smaller to left,
   * larger to right
   *
   */
  public static void rPsort2(DoublePtr x, int lo, int hi, int k) {
    boolean nalast=true;
    double v, w;
    int L, R, i, j;

    for (L = lo, R = hi; L < R; ) {
      v = x.get(k);
      for(i = L, j = R; i <= j;) {
        while (rcmp(x.get(i), v, nalast) < 0) i++;
        while (rcmp(v, x.get(j), nalast) < 0) j--;
        if (i <= j) {
          w = x.get(i);
          x.set(i++, x.get(j));
          x.set(j--,  w);
        }
      }
      if (j < k) L = i;
      if (k < i) R = j;
    }
  }

  public static void Rf_rPsort(DoublePtr x, int n, int k) {
    rPsort2(x, 0, n-1, k);
  }

  public static void R_rsort(DoublePtr x, int n) {
    Arrays.sort(x.array, x.offset, x.offset+n);
  }

  private static int rcmp(double x, double y, boolean nalast) {
    boolean nax = Double.isNaN(x), nay = Double.isNaN(y);
    if (nax && nay)     return 0;
    if (nax)            return nalast?1:-1;
    if (nay)            return nalast?-1:1;
    if (x < y)          return -1;
    if (x > y)          return 1;
    return 0;
  }
}
