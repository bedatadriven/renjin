package org.renjin.gcc;


public class RStubs {

  public static double R_NaN = 0;

  public static boolean R_finite(double x) {
    return true;
  }

  public static boolean R_IsNA(double x) {
    return false;
  }

  public static void Rf_error(String msg) {
    throw new RuntimeException(msg);
  }
}
