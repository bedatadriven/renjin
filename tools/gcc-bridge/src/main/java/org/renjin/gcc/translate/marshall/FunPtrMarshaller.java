package org.renjin.gcc.translate.marshall;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImFunctionPtrExpr;

public class FunPtrMarshaller implements Marshaller {

  @Override
  public JimpleExpr marshall(FunctionContext context, ImExpr expr) {
    if(expr.isNull()) {
      return new JimpleExpr("null");
    } else if(expr instanceof ImFunctionPtrExpr) {
      return ((ImFunctionPtrExpr) expr).invokerReference(context);
    } else {
      throw new UnsupportedOperationException(expr.toString());
    }
  }
}
