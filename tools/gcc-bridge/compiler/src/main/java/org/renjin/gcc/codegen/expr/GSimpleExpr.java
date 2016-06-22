package org.renjin.gcc.codegen.expr;


public interface GSimpleExpr extends GExpr {
  
  JExpr unwrap();
  
}
