package org.renjin.gcc.translate.expr;

import org.renjin.gcc.jimple.JimpleExpr;

/**
 * An expression to which a primitive value can be assigned.
 */
public interface PrimitiveLValue extends ImExpr {

  /**
   * Write the Jimple statements necessary to set the {@code expr}
   * to the Lvalue to which this expression evaluates.
   * @param expr a JimpleExpr matching the primitive type of this value
   */
  void writePrimitiveAssignment(JimpleExpr expr);
}
