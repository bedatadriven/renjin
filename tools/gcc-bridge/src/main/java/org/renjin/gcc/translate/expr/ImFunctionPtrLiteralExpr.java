package org.renjin.gcc.translate.expr;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImPrimitiveType;


public class ImFunctionPtrLiteralExpr extends AbstractImExpr implements ImFunctionPtrExpr {

  private final JimpleExpr expr;

  public ImFunctionPtrLiteralExpr(JimpleExpr expr) {
    this.expr = expr;
  }

  @Override
  public ImPrimitiveType type() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JimpleExpr invokerReference(FunctionContext context) {
    return expr;
  }
}
