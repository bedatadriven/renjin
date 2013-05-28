package org.renjin.gcc.translate.marshall;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImIndirectExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class PrimitiveMarshaller implements Marshaller {

  private ImPrimitiveType targetType;

  public PrimitiveMarshaller(JimpleType targetType) {
    this.targetType = ImPrimitiveType.valueOf(targetType);
  }

  @Override
  public JimpleExpr marshall(FunctionContext context, ImExpr expr) {

    // EXPERIMENT:
    // Allow implicit referencing of pointers to pass arguments by value
    if(expr instanceof ImIndirectExpr) {
      ImIndirectExpr ptr = (ImIndirectExpr) expr;
      return context.declareTemp(targetType.asJimple(),
          ptr.memref().translateToPrimitive(context, targetType));
    }

    // TODO: casting
    return expr.translateToPrimitive(context, targetType);
  }
}
