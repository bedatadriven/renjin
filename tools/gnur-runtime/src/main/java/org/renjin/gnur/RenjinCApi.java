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
package org.renjin.gnur;


import org.apache.commons.math.util.FastMath;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.FunPtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gnur.sexp.GnuSymbol;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

import java.util.Set;

/**
 * Emulation of the C API for code compiled via GCC.
 * 
 * <p>Packages compiled with previous versions of the compiler may still reference this class
 * so it shouldn't be removed.</p>
 * 
 * @deprecated Used by compiler version 0.7.xxx. See classes in org.renjin.gnur.api.
 */
@Deprecated
public class RenjinCApi {


  public static final double R_NaReal = DoubleVector.NA;

  public static final double R_NaN = Double.NaN;

  public static final double R_PosInf = Double.POSITIVE_INFINITY;

  public static final GnuSymbol R_DimSymbol = new GnuSymbol("dim")  ;
  
  public static final Set<GnuSymbol> SYMBOL_TABLE = Sets.newHashSet();

  public static void warning(BytePtr text) {
    java.lang.System.out.println(text.nullTerminatedString());
  }

  public static void error(BytePtr text) {
    throw new EvalException(text.nullTerminatedString());
  }

  public static int R_finite(double x) {
    return DoubleVector.isFinite(x) ? 1 : 0;
  }

  public static double R_pow(double x, double y) {
    return FastMath.pow(x, y);
  }

  public static double R_pow_di(double x, int n)
  {
    double xn = 1.0;
    
    if(Double.isNaN(x)) {
      return x;
    }
    if(IntVector.isNA(n)) {
      return DoubleVector.NA;
    }
    if (n != 0) {
      if (!DoubleVector.isFinite(x)) {
        return R_pow(x, (double)n);
      }
      boolean isNegative = (n < 0);
      if(isNegative) {
        n = -n;
      }
      for(;;) {
        if( (n & 01) != 0 ) {
          xn *= x;
        }
        n >>= 1;
        if( n != 0) {
          x *= x;
        } else {
          break;
        }
      }
      if(isNegative) {
        xn = 1d / (double)xn;
      }
    }
    return xn;
  }


  public static int TYPEOF(SEXP sexp) {
    throw new UnsupportedOperationException();
  }

  
  public static void Rf_warning(BytePtr text) {
    System.err.println(text.nullTerminatedString());
  }
  
  public static BytePtr gettext(BytePtr message) {
    return message;
  }
  
  public static BytePtr dgettext(BytePtr packageName, BytePtr message) {
    return message;
  }

  public static void Rf_error(BytePtr text) {
    throw new RuntimeException(text.nullTerminatedString());
  }

  public static int R_IsNA(double x) {
    return DoubleVector.isNA(x) ? 1 : 0;
  }

  public static void debugij(int i, int j) {
    System.out.println("i = " + i + ", j = " + j);
  }

  public static void debugi(int i) {
    System.out.println("i = " + i);
  }

  public static void debugiter(int i) {
    System.out.println("iter = " + i);
  }



  public static void debug(String str) {
    System.out.println(str);
  }
  
  public static int LENGTH(SEXP x) {
    throw new UnsupportedOperationException();
  }
  
 
  public static DoublePtr REAL(SEXP x) {
    throw new UnsupportedOperationException();
  }
  
  
  public static SEXP Rf_coerceVector(SEXP x, int type) {
    throw new UnsupportedOperationException();
  }
  
  public static SEXP Rf_protect(SEXP obj) {
    // NOOP
    return obj;
  }
  
  public static void Rf_unprotect(int count) {
    // NOOP
  }
  
  public static SEXP Rf_allocVector(int type, int size) {
    return null;
  }
  
  public static void R_CheckUserInterrupt() {
    
  }
 
  // fortran calling convention
  public static void rwarn_(BytePtr message, int charCount)  {
    // TODO: we really need the R context here
    System.err.println(message.nullTerminatedString());
  }

  public static void GetRNGstate() {

  }

  public static void PutRNGstate() {

  }
  
  public static double unif_rand() {
    // This is spike to get other things working, but to correctly implement, this
    // needs to be tied somehow to the Random number generator in the calling context
    return Math.random();
  }
  
  public static void vmmin(int n0, DoublePtr b, DoublePtr fmin, FunPtr fminfn, FunPtr fmingr, int maxit, int trace,
                          IntPtr mask, double abstol, double reltol, int nREPORT, Object ex, IntPtr fncount,
                          IntPtr grcount, IntPtr fail) {
    throw new UnsupportedOperationException("not implemented");
  }



}

