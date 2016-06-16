package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;

/**
 * Generic interface to generation of load/stores for either local variables or static fields
 */
public interface JLValue extends JExpr {
  
  void store(MethodGenerator mv, JExpr expr);
  
}
