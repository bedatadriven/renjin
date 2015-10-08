// Initial template generated from PrtUtil.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

@SuppressWarnings("unused")
public final class PrtUtil {

  private PrtUtil() { }



  public static void Rf_formatLogical(IntPtr p0, /*R_xlen_t*/ int p1, IntPtr p2) {
     throw new UnimplementedGnuApiMethod("Rf_formatLogical");
  }

  public static void Rf_formatInteger(IntPtr p0, /*R_xlen_t*/ int p1, IntPtr p2) {
     throw new UnimplementedGnuApiMethod("Rf_formatInteger");
  }

  public static void Rf_formatReal(DoublePtr p0, /*R_xlen_t*/ int p1, IntPtr p2, IntPtr p3, IntPtr p4, int p5) {
     throw new UnimplementedGnuApiMethod("Rf_formatReal");
  }

  // void Rf_formatComplex (Rcomplex *, R_xlen_t, int *, int *, int *, int *, int *, int *, int)

  public static CharPtr Rf_EncodeLogical(int p0, int p1) {
     throw new UnimplementedGnuApiMethod("Rf_EncodeLogical");
  }

  public static CharPtr Rf_EncodeInteger(int p0, int p1) {
     throw new UnimplementedGnuApiMethod("Rf_EncodeInteger");
  }

  public static CharPtr Rf_EncodeReal0(double p0, int p1, int p2, int p3, CharPtr p4) {
     throw new UnimplementedGnuApiMethod("Rf_EncodeReal0");
  }

  // const char* Rf_EncodeComplex (Rcomplex, int, int, int, int, int, int, const char *)

  // const char* Rf_EncodeReal (double, int, int, int, char)

  public static int IndexWidth(/*R_xlen_t*/ int p0) {
     throw new UnimplementedGnuApiMethod("IndexWidth");
  }

  public static void Rf_VectorIndex(/*R_xlen_t*/ int p0, int p1) {
     throw new UnimplementedGnuApiMethod("Rf_VectorIndex");
  }

  public static void Rf_printIntegerVector(IntPtr p0, /*R_xlen_t*/ int p1, int p2) {
     throw new UnimplementedGnuApiMethod("Rf_printIntegerVector");
  }

  public static void Rf_printRealVector(DoublePtr p0, /*R_xlen_t*/ int p1, int p2) {
     throw new UnimplementedGnuApiMethod("Rf_printRealVector");
  }

  // void Rf_printComplexVector (Rcomplex *, R_xlen_t, int)
}
