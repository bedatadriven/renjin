/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gnur;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;

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
  public static void iPsort2(Ptr x, int lo, int hi, int k) {
    boolean nalast = true;
    int v, w;
    int L, R, i, j;

    for (L = lo, R = hi; L < R;) {
      v = x.getAlignedInt(k);
      for (i = L, j = R; i <= j;) {
        while (icmp(x.getAlignedInt(i), v, nalast) < 0) {
          i++;
        }
        while (icmp(v, x.getAlignedInt(j), nalast) < 0) {
          j--;
        }
        if (i <= j) {
          w = x.getAlignedInt(i);
          x.setAlignedInt(i++, x.getAlignedInt(j));
          x.setAlignedInt(j--, w);
        }
      }
      if (j < k) {
        L = i;
      }
      if (k < i) {
        R = j;
      }
    }
  }

  public static void rPsort2(Ptr x, int lo, int hi, int k) {
    boolean nalast=true;
    double v, w;
    int L, R, i, j;

    for (L = lo, R = hi; L < R; ) {
      v = x.getAlignedDouble(k);
      for(i = L, j = R; i <= j;) {
        while (rcmp(x.getAlignedDouble(i), v, nalast) < 0) {
          i++;
        }
        while (rcmp(v, x.getAlignedDouble(j), nalast) < 0) {
          j--;
        }
        if (i <= j) {
          w = x.getAlignedDouble(i);
          x.setAlignedDouble(i++, x.getAlignedDouble(j));
          x.setAlignedDouble(j--, w);
        }
      }
      if (j < k) {
        L = i;
      }
      if (k < i) {
        R = j;
      }
    }
  }

  public static void Rf_iPsort(Ptr x, int n, int k) {
    iPsort2(x, 0, n - 1, k);
  }

  public static void Rf_rPsort(Ptr x, int n, int k) {
    rPsort2(x, 0, n - 1, k);
  }

  public static void R_isort(IntPtr x, int n) {
    Arrays.sort(x.array, x.offset, x.offset + n);
  }

  public static void R_rsort(DoublePtr x, int n) {
    Arrays.sort(x.array, x.offset, x.offset + n);
  }

  private static int icmp(int x, int y, boolean nalast) {
    boolean nax = IntVector.isNA(x), nay = IntVector.isNA(y);
    if (nax && nay) {
      return 0;
    }
    if (nax) {
      return nalast ? 1 : -1;
    }
    if (nay) {
      return nalast ? -1 : 1;
    }
    if (x < y) {
      return -1;
    }
    if (x > y) {
      return 1;
    }
    return 0;
  }

  public static int rcmp(double x, double y, boolean nalast) {
    boolean nax = DoubleVector.isNA(x), nay = DoubleVector.isNA(y);
    if (nax && nay) {
      return 0;
    }
    if (nax) {
      return nalast ? 1 : -1;
    }
    if (nay) {
      return nalast ? -1 : 1;
    }
    if (x < y) {
      return -1;
    }
    if (x > y) {
      return 1;
    }
    return 0;
  }
}
