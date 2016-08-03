package org.renjin.gcc.codegen.expr;

/**
 * Pointer type backed by a JVM reference.
 */
public interface RefPtrExpr extends GSimpleExpr, PtrExpr {
  
  JExpr unwrap();
}
