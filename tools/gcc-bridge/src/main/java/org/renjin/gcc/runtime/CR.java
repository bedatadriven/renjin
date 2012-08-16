package org.renjin.gcc.runtime;


import org.apache.commons.math.util.FastMath;
import org.renjin.eval.EvalException;
import org.renjin.gcc.translate.MethodTable;
import org.renjin.sexp.DoubleVector;

/**
 * Emulation of the C-R's internal API
 */
public class CR {

  public static final double R_NaReal = DoubleVector.NA;

  public static void warning(String text) {
    java.lang.System.out.println(text);
  }

  public static void error(String text) {
    throw new EvalException(text);
  }


  public static void init(MethodTable table) {
    table.addMethod("R_finite", DoubleVector.class, "isFinite");
    table.addMethod("R_pow", FastMath.class, "pow");
    table.addReferenceClass(CR.class);
  }

  public static void R_registerRoutines(Object dll, Object CEntries, Object callEntries, Object fortEntries, int count) {

  }

  public static void R_useDynamicSymbols(Object dll, int count) {

  }
}
