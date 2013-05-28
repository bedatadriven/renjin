package org.renjin.gcc.translate.expr;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImPrimitiveType;


public class JvmExprs {


  /**
   *
   * @param expr
   * @param type
   * @param simple
   * @return
   */
  public static ImExpr toExpr(FunctionContext context, JimpleExpr expr, JimpleType type, boolean simple) {
    if(type.isPrimitive()) {
      return new ImLiteralPrimitiveExpr(expr, ImPrimitiveType.valueOf(type));
    } else if(type.isPointerWrapper()) {
      return new ImWrappedPtrExpr(ensureSimple(context, expr, type, simple), type);
    } else if(type.isFunctionPointer()) {
      return new ImFunctionPtrLiteralExpr(ensureSimple(context, expr, type, simple));
    } else {
      return new ImObjectRefExpr(expr, type);
    }
  }

  private static JimpleExpr ensureSimple(FunctionContext context, JimpleExpr expr, JimpleType type, boolean simple) {
    if(simple) {
      return expr;
    } else {
      String tmp = context.declareTemp(type);
      context.getBuilder().addStatement(tmp + " = " + expr);
      return new JimpleExpr(tmp);
    }
  }
}
