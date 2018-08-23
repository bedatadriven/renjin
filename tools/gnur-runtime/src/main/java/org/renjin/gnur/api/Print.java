/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
// Initial template generated from Print.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.*;
import org.renjin.sexp.SEXP;

@SuppressWarnings("unused")
public final class Print {

  private Print() { }

  public static Ptr R_print = MixedPtr.malloc(13 * 4);

  public static void Rprintf(BytePtr format, Object... formatArgs) {
    Stdlib.printf(format, formatArgs);
  }

  public static void REprintf(BytePtr format, Object... formatArgs) {
    Stdlib.printf(format, formatArgs);
  }

  public static void Rf_formatRaw(BytePtr p0, /*R_xlen_t*/ int p1, IntPtr p2) {
    throw new UnimplementedGnuApiMethod("Rf_formatRaw");
  }

  // void Rf_formatString (SEXP *, R_xlen_t, int *, int)

  public static BytePtr Rf_EncodeElement0(SEXP p0, int p1, int p2, BytePtr p3) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeElement0");
  }

  public static BytePtr Rf_EncodeEnvironment(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeEnvironment");
  }

  // const char* Rf_EncodeElement (SEXP, int, int, char)

  public static void Rf_printArray(SEXP p0, SEXP p1, int p2, int p3, SEXP p4) {
    throw new UnimplementedGnuApiMethod("Rf_printArray");
  }

  public static void Rf_printMatrix(SEXP p0, int p1, SEXP p2, int p3, int p4, SEXP p5, SEXP p6, BytePtr p7, BytePtr p8) {
    throw new UnimplementedGnuApiMethod("Rf_printMatrix");
  }

  public static void Rf_printNamedVector(SEXP p0, SEXP p1, int p2, BytePtr p3) {
    throw new UnimplementedGnuApiMethod("Rf_printNamedVector");
  }

  public static void Rf_printVector(SEXP p0, int p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_printVector");
  }

  public static int dblepr0(BytePtr p0, IntPtr p1, DoublePtr p2, IntPtr p3) {
    throw new UnimplementedGnuApiMethod("dblepr0");
  }

  public static int intpr0(BytePtr p0, IntPtr p1, IntPtr p2, IntPtr p3) {
    throw new UnimplementedGnuApiMethod("intpr0");
  }

  // int realpr0 (const char *, int *, float *, int *)

  public static void R_PV(SEXP s) {
    throw new UnimplementedGnuApiMethod("R_PV");
  }
}
