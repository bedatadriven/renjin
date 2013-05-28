package org.renjin.gcc.translate.call;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.marshall.Marshallers;

/**
 * A simple call parameter consisting of a single JVM parameter
 * mapped to a single gimple parameter
 */
public class SimpleParam implements CallParam {
  private final JimpleType type;

  public SimpleParam(JimpleType type) {
    this.type = type;
  }

  @Override
  public JimpleExpr marshall(FunctionContext context, ImExpr expr) {
    return Marshallers.marshall(context, expr, type);
  }
}
