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
// Initial template generated from PrtUtil.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
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

  public static BytePtr Rf_EncodeLogical(int p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeLogical");
  }

  public static BytePtr Rf_EncodeInteger(int p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeInteger");
  }

  public static BytePtr Rf_EncodeReal0(double p0, int p1, int p2, int p3, BytePtr p4) {
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
