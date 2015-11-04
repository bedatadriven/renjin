// Initial template generated from Arith.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class Arith {

  private Arith() { }


  public static int R_IsNA(double p0) {
    return DoubleVector.isNA(p0) ? 1 : 0;
  }

  public static int R_IsNaN(double p0) {
    return DoubleVector.isNaN(p0) ? 1 : 0;
  }

  public static int R_finite(double p0) {
    return DoubleVector.isFinite(p0) ? 1 : 0;
  }
}
