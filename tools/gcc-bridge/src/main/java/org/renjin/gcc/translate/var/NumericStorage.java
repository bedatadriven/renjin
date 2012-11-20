package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.JimpleExpr;

/**
 * Responsible for writing the jimple instructions to store and retrieve the value of 
 * a single numeric value (double, float, int, boolean)
 *
 */
public interface NumericStorage {

  void assign(JimpleExpr expr);

  JimpleExpr asNumericExpr();

  JimpleExpr addressOf();

}
