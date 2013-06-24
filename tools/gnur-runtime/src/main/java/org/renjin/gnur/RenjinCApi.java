package org.renjin.gnur;


import org.apache.commons.math.util.FastMath;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;

/**
 * Emulation of the C API for code compiled via GCC
 */
public class RenjinCApi {


  public static final double R_NaReal = DoubleVector.NA;

  public static final double R_NaN = Double.NaN;

  public static final double R_PosInf = Double.POSITIVE_INFINITY;

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

  public static void R_registerRoutines(Object dll, Object CEntries, Object callEntries, Object fortEntries, int count) {

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
  
  public static double[] REAL(SEXP x) {
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
}

