package org.renjin.gcc.codegen.type;


import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

public interface SimpleTypeStrategy<ExprT extends GExpr> extends TypeStrategy<ExprT> {

  Type getJvmType();
  
  ExprT wrap(JExpr expr);
  
}
