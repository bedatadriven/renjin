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
}
