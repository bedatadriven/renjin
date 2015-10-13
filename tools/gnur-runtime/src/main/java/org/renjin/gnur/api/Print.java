// Initial template generated from Print.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class Print {

  private Print() { }



  public static void Rf_formatRaw(BytePtr p0, /*R_xlen_t*/ int p1, IntPtr p2) {
     throw new UnimplementedGnuApiMethod("Rf_formatRaw");
  }

  // void Rf_formatString (SEXP *, R_xlen_t, int *, int)

  public static CharPtr Rf_EncodeElement0(SEXP p0, int p1, int p2, CharPtr p3) {
     throw new UnimplementedGnuApiMethod("Rf_EncodeElement0");
  }

  public static CharPtr Rf_EncodeEnvironment(SEXP p0) {
     throw new UnimplementedGnuApiMethod("Rf_EncodeEnvironment");
  }

  // const char* Rf_EncodeElement (SEXP, int, int, char)

  public static void Rf_printArray(SEXP p0, SEXP p1, int p2, int p3, SEXP p4) {
     throw new UnimplementedGnuApiMethod("Rf_printArray");
  }

  public static void Rf_printMatrix(SEXP p0, int p1, SEXP p2, int p3, int p4, SEXP p5, SEXP p6, CharPtr p7, CharPtr p8) {
     throw new UnimplementedGnuApiMethod("Rf_printMatrix");
  }

  public static void Rf_printNamedVector(SEXP p0, SEXP p1, int p2, CharPtr p3) {
     throw new UnimplementedGnuApiMethod("Rf_printNamedVector");
  }

  public static void Rf_printVector(SEXP p0, int p1, int p2) {
     throw new UnimplementedGnuApiMethod("Rf_printVector");
  }

  public static int dblepr0(CharPtr p0, IntPtr p1, DoublePtr p2, IntPtr p3) {
     throw new UnimplementedGnuApiMethod("dblepr0");
  }

  public static int intpr0(CharPtr p0, IntPtr p1, IntPtr p2, IntPtr p3) {
     throw new UnimplementedGnuApiMethod("intpr0");
  }

  // int realpr0 (const char *, int *, float *, int *)

  public static void R_PV(SEXP s) {
     throw new UnimplementedGnuApiMethod("R_PV");
  }
}
