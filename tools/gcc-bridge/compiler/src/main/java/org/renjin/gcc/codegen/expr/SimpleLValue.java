package org.renjin.gcc.codegen.expr;

/**
 * Generic interface to generation of load/stores for either local variables or static fields
 */
public interface SimpleLValue extends SimpleExpr, LValue<SimpleExpr> {
  
}
