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
// Initial template generated from Utils.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.eval.EvalException;
import org.renjin.gcc.annotations.Noop;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.primitives.files.Files;

import java.nio.charset.StandardCharsets;

import static org.renjin.gnur.Sort.rcmp;

@SuppressWarnings("unused")
public final class Utils {

  private Utils() { }



  public static void R_isort(IntPtr p0, int p1) {
    org.renjin.gnur.Sort.R_isort(p0, p1);
  }

  public static void R_rsort(DoublePtr p0, int p1) {
    org.renjin.gnur.Sort.R_rsort(p0, p1);
  }

  // void R_csort (Rcomplex *, int)

  public static void rsort_with_index(DoublePtr x, IntPtr indx, int n) {
    double v;
    int i, j, h, iv;

    h = 1;
    boolean loop = true;
    while(loop) {
      if (h <= n / 9) {
        loop = false;
      }
      h = 3 * h + 1;
    }

    for (; h > 0; h /= 3) {
      for (i = h; i < n; i++) {
        v = x.getDouble(i);
        iv = indx.getInt(i);
        j = i;
        while (j >= h && rcmp(x.getDouble(j - h), v, true) > 0) {
          x.set(j, x.getDouble(j - h));
          indx.setInt(j, indx.getInt(j - h));
          j -= h;
        }
        x.set(j, v);
        indx.setInt(j, iv);
      }
    }
  }

  public static void Rf_revsort(DoublePtr p0, IntPtr p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_revsort");
  }

  public static void Rf_iPsort(IntPtr p0, int p1, int p2) {
    org.renjin.gnur.Sort.Rf_iPsort(p0, p1, p2);
  }

  public static void Rf_rPsort(DoublePtr p0, int p1, int p2) {
    org.renjin.gnur.Sort.Rf_rPsort(p0, p1, p2);
  }

  // void Rf_cPsort (Rcomplex *, int, int)

  public static void R_qsort(DoublePtr v, /*size_t*/ int i, /*size_t*/ int j) {
    org.renjin.gnur.qsort.R_qsort(v, i, j);
  }

  public static void R_qsort_I(DoublePtr v, IntPtr II, int i, int j) {
    org.renjin.gnur.qsort.R_qsort_I(v, II, i, j);
  }

  public static void R_qsort_int(IntPtr iv, /*size_t*/ int i, /*size_t*/ int j) {
    org.renjin.gnur.qsort.R_qsort_int(iv, i, j);
  }

  public static void R_qsort_int_I(IntPtr iv, IntPtr II, int i, int j) {
    org.renjin.gnur.qsort.R_qsort_int_I(iv, II, i, j);
  }

  public static BytePtr R_ExpandFileName(BytePtr p0) {
    return BytePtr.nullTerminatedString(Files.pathExpand(p0.nullTerminatedString()), StandardCharsets.UTF_8);
  }

  public static void Rf_setIVector(IntPtr p0, int p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_setIVector");
  }

  public static void Rf_setRVector(DoublePtr p0, int p1, double p2) {
    throw new UnimplementedGnuApiMethod("Rf_setRVector");
  }

  public static boolean Rf_StringFalse(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_StringFalse");
  }

  public static boolean Rf_StringTrue(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_StringTrue");
  }

  public static boolean Rf_isBlankString(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_isBlankString");
  }

  @Deprecated
  public static double R_atof(BytePtr str) {
    return Defn.R_atof((Ptr)str);
  }

  @Deprecated
  public static double R_atof(Ptr str) {
    return Defn.R_atof(str);
  }

  // double R_strtod (const char *c, char **end)

  public static BytePtr R_tmpnam(BytePtr prefix, BytePtr tempdir) {
    throw new UnimplementedGnuApiMethod("R_tmpnam");
  }

  public static BytePtr R_tmpnam2(BytePtr prefix, BytePtr tempdir, BytePtr fileext) {
    throw new UnimplementedGnuApiMethod("R_tmpnam2");
  }

  public static void R_CheckUserInterrupt() {
    if(Thread.interrupted()) {
      throw new EvalException("Interrupted.");
    }
  }

  @Noop
  public static void R_CheckStack() {
    // Noop: JVM will throw a StackOverflowError for us if need be
  }

  @Noop
  public static void R_CheckStack2(/*size_t*/ int p0) {
    // Noop: JVM will throw a StackOverflowError for us if need be
  }

  public static int findInterval(DoublePtr xt, int n, double x, boolean rightmost_closed, boolean all_inside, int ilo, IntPtr mflag) {
    throw new UnimplementedGnuApiMethod("findInterval");
  }

  public static void find_interv_vec(DoublePtr xt, IntPtr n, DoublePtr x, IntPtr nx, IntPtr rightmost_closed, IntPtr all_inside, IntPtr indx) {
    throw new UnimplementedGnuApiMethod("find_interv_vec");
  }

  public static void R_max_col(DoublePtr matrix, IntPtr nr, IntPtr nc, IntPtr maxes, IntPtr ties_meth) {
    throw new UnimplementedGnuApiMethod("R_max_col");
  }
}
