package org.renjin.gcc.codegen.var;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;

/**
 * Generator for Left Hand Side (LHS) expressions
 */
public interface Lhs <T extends ExprGenerator> {
  
  void store(MethodGenerator mv, T rhs);
}
