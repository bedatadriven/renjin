package org.renjin.gcc.codegen.expr;

/**
 * Expression whose address can be retrieved
 */
public interface Addressable extends Expr {
  
  Expr addressOf();
  
}
