package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.var.Value;

/**
 * Expression generator for pointer expressions
 */
public interface PtrExpr extends ExprGenerator {

  /**
   * 
   * @return a generator for the value pointed to by this expression
   */
  ExprGenerator valueOf();

  /**
   * 
   * @param offset the offset relative to the current pointer, <strong>in elements, not bytes!</strong>
   */
  PtrExpr pointerPlus(Value offset);
  
}
