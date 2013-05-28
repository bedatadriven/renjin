package org.renjin.gcc.translate.expr;


import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.PrimitiveType;

/**
 * Represents a primitive expression that has already been translated to Jimple
 */
public class ImLiteralPrimitiveExpr extends AbstractImExpr {

  private ImPrimitiveType type;
  private JimpleExpr expr;

  public ImLiteralPrimitiveExpr(JimpleExpr expr, ImPrimitiveType type) {
    this.expr = expr;
    this.type = type;
  }

  @Override
  public ImPrimitiveType type() {
    return type;
  }

  @Override
  public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
    if(this.type != type) {
      throw new UnsupportedOperationException(this + " (" + this.type + ") => " + type.toString());
    }
    return expr;
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
