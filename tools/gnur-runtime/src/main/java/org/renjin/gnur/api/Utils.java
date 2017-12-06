/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

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

  public static void rsort_with_index(DoublePtr p0, IntPtr p1, int p2) {
    throw new UnimplementedGnuApiMethod("rsort_with_index");
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

  @Deprecated
  public static CharPtr R_ExpandFileName(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_ExpandFileName");
  }

  public static BytePtr R_ExpandFileName(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("R_ExpandFileName");
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

  public static double R_atof(BytePtr str) {
    throw new UnimplementedGnuApiMethod("R_atof");
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

  public static void R_CheckStack() {
    // Noop: JVM will throw a StackOverflowError for us if need be
  }

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
