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
// Initial template generated from PrtUtil.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.DoubleVector;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@SuppressWarnings("unused")
public final class PrtUtil {

  private PrtUtil() { }



  public static BytePtr Rf_EncodeLogical(int p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeLogical");
  }

  public static BytePtr Rf_EncodeInteger(int p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeInteger");
  }

  public static BytePtr Rf_EncodeReal0(double x, int w, int d, int e, BytePtr dec) {
    String result;
    /* IEEE allows signed zeros (yuck!) */
    if (x == 0.0) {
      x = 0.0;
    }
    if (!Double.isFinite(x)) {
      if(DoubleVector.isNA(x)) {
        result = "NA";
      } else if(DoubleVector.isNaN(x)) {
        result = "NaN";
      } else if(x > 0) {
        result = "Inf";
      } else {
        result = "-Inf";
      }
    } else if (e != 0) {

      // Use scientific notation

      String format;
      if(d != 0) {
        if(w == 0) {
          format = String.format("%%#.%de", d);
        } else {
          format = String.format("%%#%d.%de", w, d);
        }
      } else {
        if(w == 0) {
          format = String.format("%%.%de", d);
        } else {
          format = String.format("%%%d.%de", w, d);
        }
      }

      result = String.format(format, x);

    } else {

      // e=0, do not use scientific notation

      NumberFormat format = DecimalFormat.getNumberInstance();
      format.setMinimumFractionDigits(d);
      format.setGroupingUsed(false);
      result = format.format(x);
    }

    return BytePtr.nullTerminatedString(Strings.padStart(result, w, ' '), Charsets.UTF_8);
  }

// printutils.c
//  public static Ptr Rf_EncodeComplex (double x_r, double x_i, int wr, int dr, int er, int wi, int di, int ei, Ptr dec) {
//    throw new UnimplementedGnuApiMethod("Rf_EncodeComplex");
//  }

  public static Ptr Rf_EncodeReal (double x, int w, int d, int e, char cdec) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeReal");
  }

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
