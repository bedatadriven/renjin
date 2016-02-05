package org.renjin.gcc.codegen.expr;

/**
 * Expression whose address can be retreived
 */
public interface Addressable extends ExprGenerator {
  
  ExprGenerator addressOf();
  
}
