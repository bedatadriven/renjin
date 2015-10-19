package org.renjin.gcc.codegen.expr;

/**
 * Interface for ExprGenerators which are addressable
 */
public interface AddressableGenerator extends ExprGenerator {
  
  ExprGenerator addressOf();
  
}
