package org.renjin.gnur;


import com.google.common.collect.Sets;
import org.apache.commons.math.util.FastMath;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.FunPtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gnur.sexp.GnuSymbol;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

import java.util.Set;

/**
 * Emulation of the C API for code compiled via GCC
 */
public class RenjinCApi {


  public static final double R_NaReal = DoubleVector.NA;

  public static final double R_NaN = Double.NaN;

  public static final double R_PosInf = Double.POSITIVE_INFINITY;

  public static final GnuSymbol R_DimSymbol = new GnuSymbol("dim");

  public static final Set<GnuSymbol> SYMBOL_TABLE = Sets.newHashSet();

  public static void warning(CharPtr text) {
    java.lang.System.out.println(text.asString());
  }

  public static void error(CharPtr text) {
    throw new EvalException(text.asString());
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

  public static void R_registerRoutines(Object dll, Object CEntries, Object callEntries, Object q, int count) {

  }

  public static void R_useDynamicSymbols(Object dll, int count) {

  }

  public static void Rf_warning(CharPtr text) {
    System.err.println(text.asString());
  }
  
  public static CharPtr gettext(CharPtr message) {
    return message;
  }
  
  public static CharPtr dgettext(CharPtr packageName, CharPtr message) {
    return message;
  }

  public static void Rf_error(CharPtr text) {
    throw new RuntimeException(text.asString());
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

  public static void debug_var(CharPtr varName, double x) {
    System.out.println(varName + " = " + x);

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
  public static void rwarn_(CharPtr message, int charCount)  {
    // TODO: we really need the R context here
    System.err.println(message.asString());
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

  
  public static int Rf_length(SEXP sexp) {
    return sexp.length();
  }

}

