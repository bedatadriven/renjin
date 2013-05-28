package org.renjin.gcc.translate.marshall;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.PtrWrapperUtils;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImIndirectExpr;

public class PointerWrapperMarshaller implements Marshaller {
  
  @Override
  public JimpleExpr marshall(FunctionContext context, ImExpr expr) {
    if(expr instanceof ImIndirectExpr) {
      return PtrWrapperUtils.wrapPointer(context, (ImIndirectExpr) expr);
    }
    throw new UnsupportedOperationException(expr.toString() + " (" + expr.getClass().getSimpleName() + ")");
  }
}
