package org.renjin.gcc.translate.expr;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;

/**
 * An intermediate expression representing a reference
 * to an arbitrary JVM object.
 */
public class ImObjectRefExpr extends AbstractImExpr {

  private JimpleExpr expr;
  private JimpleType type;

  public ImObjectRefExpr(JimpleExpr expr, JimpleType type) {
    this.expr = expr;
    this.type = type;
  }

  @Override
  public JimpleExpr translateToObjectReference(FunctionContext context, JimpleType className) {
    if(className.equals(type)) {
      return expr;
    }
    return super.translateToObjectReference(context, className);
  }

  @Override
  public ImType type() {
    throw new UnsupportedOperationException();
  }
}
