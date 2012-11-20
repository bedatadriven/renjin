package org.renjin;


import org.apache.commons.math.util.FastMath;
import org.renjin.eval.EvalException;
import org.renjin.sexp.DoubleVector;

/**
 * Emulation of the C API for code compiled via GCC
 */
public class RenjinCApi {


  public static final double R_NaReal = DoubleVector.NA;

  public static void warning(String text) {
    java.lang.System.out.println(text);
  }

  public static void error(String text) {
    throw new EvalException(text);
  }

  public static boolean R_finite(double x) {
    return DoubleVector.isFinite(x);
  }

  public static double R_pow(double x, double y) {
    return FastMath.pow(x, y);
  }

  public static void R_registerRoutines(Object dll, Object CEntries, Object callEntries, Object fortEntries, int count) {

  }

  public static void R_useDynamicSymbols(Object dll, int count) {

  }

  public static void Rf_warning(String text) {
    System.err.println(text);
  }

  public static void Rf_error(String text) {
    throw new RuntimeException(text);
  }

  public static boolean R_IsNA(double x) {
    return DoubleVector.isNA(x);
  }

  public static void debugij(int i, int j) {
    System.out.println("i = " + i + ", j = " + j);
  }

  public static void debug(String str) {
    System.out.println(str);
  }

  public static void debug_var(String varName, double x) {
    System.out.println(varName + " = " + x);

  }
  
 
  // fortran calling convention
  public static void rwarn(String message, int charCount)  {
    // TODO: we really need the R context here
    System.err.println(message);
  }
}

