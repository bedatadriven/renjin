package org.renjin.gcc.gimple.expr;

/**
 * A GimpleExpr which evaluates to the real part of a complex number
 */
public class GimpleRealPartExpr extends GimpleComplexPartExpr {

  @Override
  public String toString() {
    return "Re(" + getComplexValue() + ")";
  }
}
