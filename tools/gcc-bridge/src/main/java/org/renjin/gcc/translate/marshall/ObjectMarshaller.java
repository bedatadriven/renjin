package org.renjin.gcc.translate.marshall;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;


public class ObjectMarshaller implements Marshaller {
  private JimpleType className;

  public ObjectMarshaller(JimpleType className) {
    this.className = className;
  }

  @Override
  public JimpleExpr marshall(FunctionContext context, ImExpr expr) {
    return expr.translateToObjectReference(context, className);
  }
}
