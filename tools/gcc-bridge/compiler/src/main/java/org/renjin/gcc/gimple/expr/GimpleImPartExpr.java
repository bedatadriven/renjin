package org.renjin.gcc.gimple.expr;

/**
 * A {@code GimpleExpr} which evaluates to the imaginary part of a complex number
 */
public class GimpleImPartExpr extends GimpleComplexPartExpr {

  @Override
  public String toString() {
    return "Im(" + getComplexValue() + ")";
  }
}
