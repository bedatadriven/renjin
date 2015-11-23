// Initial template generated from Utils.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

@SuppressWarnings("unused")
public final class Utils {

  private Utils() { }



  public static void R_isort(IntPtr p0, int p1) {
     throw new UnimplementedGnuApiMethod("R_isort");
  }

  public static void R_rsort(DoublePtr p0, int p1) {
     throw new UnimplementedGnuApiMethod("R_rsort");
  }

  // void R_csort (Rcomplex *, int)

  public static void rsort_with_index(DoublePtr p0, IntPtr p1, int p2) {
     throw new UnimplementedGnuApiMethod("rsort_with_index");
  }

  public static void Rf_revsort(DoublePtr p0, IntPtr p1, int p2) {
     throw new UnimplementedGnuApiMethod("Rf_revsort");
  }

  public static void Rf_iPsort(IntPtr p0, int p1, int p2) {
     throw new UnimplementedGnuApiMethod("Rf_iPsort");
  }

  public static void Rf_rPsort(DoublePtr p0, int p1, int p2) {
     throw new UnimplementedGnuApiMethod("Rf_rPsort");
  }

  // void Rf_cPsort (Rcomplex *, int, int)

  public static void R_qsort(DoublePtr v, /*size_t*/ int i, /*size_t*/ int j) {
     throw new UnimplementedGnuApiMethod("R_qsort");
  }

  public static void R_qsort_I(DoublePtr v, IntPtr II, int i, int j) {
     throw new UnimplementedGnuApiMethod("R_qsort_I");
  }

  public static void R_qsort_int(IntPtr iv, /*size_t*/ int i, /*size_t*/ int j) {
     throw new UnimplementedGnuApiMethod("R_qsort_int");
  }

  public static void R_qsort_int_I(IntPtr iv, IntPtr II, int i, int j) {
     throw new UnimplementedGnuApiMethod("R_qsort_int_I");
  }

  public static CharPtr R_ExpandFileName(CharPtr p0) {
     throw new UnimplementedGnuApiMethod("R_ExpandFileName");
  }

  public static void Rf_setIVector(IntPtr p0, int p1, int p2) {
     throw new UnimplementedGnuApiMethod("Rf_setIVector");
  }

  public static void Rf_setRVector(DoublePtr p0, int p1, double p2) {
     throw new UnimplementedGnuApiMethod("Rf_setRVector");
  }

  public static boolean Rf_StringFalse(CharPtr p0) {
     throw new UnimplementedGnuApiMethod("Rf_StringFalse");
  }

  public static boolean Rf_StringTrue(CharPtr p0) {
     throw new UnimplementedGnuApiMethod("Rf_StringTrue");
  }

  public static boolean Rf_isBlankString(CharPtr p0) {
     throw new UnimplementedGnuApiMethod("Rf_isBlankString");
  }

  public static double R_atof(CharPtr str) {
     throw new UnimplementedGnuApiMethod("R_atof");
  }

  // double R_strtod (const char *c, char **end)

  public static CharPtr R_tmpnam(CharPtr prefix, CharPtr tempdir) {
     throw new UnimplementedGnuApiMethod("R_tmpnam");
  }

  public static CharPtr R_tmpnam2(CharPtr prefix, CharPtr tempdir, CharPtr fileext) {
     throw new UnimplementedGnuApiMethod("R_tmpnam2");
  }

  public static void R_CheckUserInterrupt() {
     if(Thread.interrupted()) {
       throw new EvalException("Interrupted.");
     }
  }

  public static void R_CheckStack() {
     throw new UnimplementedGnuApiMethod("R_CheckStack");
  }

  public static void R_CheckStack2(/*size_t*/ int p0) {
     throw new UnimplementedGnuApiMethod("R_CheckStack2");
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
