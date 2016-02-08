package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;

/**
 * Generator for expressions to which other values can be assigned
 */
public interface LValue<T extends Expr> {
  
  void store(MethodGenerator mv, T rhs);
}
