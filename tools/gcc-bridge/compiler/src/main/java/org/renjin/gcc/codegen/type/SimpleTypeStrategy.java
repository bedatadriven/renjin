package org.renjin.gcc.codegen.type;


import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;

public interface SimpleTypeStrategy<ExprT extends GExpr> extends TypeStrategy<ExprT> {

  Type getJvmType();
  
  ExprT wrap(JExpr expr);
  
}
