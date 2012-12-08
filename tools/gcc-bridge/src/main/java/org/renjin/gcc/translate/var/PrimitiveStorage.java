package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.JimpleExpr;

/**
 * Responsible for writing the jimple instructions to store and retrieve the value of 
 * a single numeric value (double, float, int, boolean)
 *
 */
public interface PrimitiveStorage {

  void assign(JimpleExpr expr);

  JimpleExpr asNumericExpr();

  /**
   * 
   * @return an expression that evaluates to newly created {@link Ptr} to this 
   * primitive value
   */
  JimpleExpr wrapPointer();

}
