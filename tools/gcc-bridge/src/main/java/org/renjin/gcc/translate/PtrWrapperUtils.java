package org.renjin.gcc.translate;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.expr.ArrayRef;
import org.renjin.gcc.translate.expr.ImIndirectExpr;

public class PtrWrapperUtils {

  public static JimpleExpr wrapPointer(FunctionContext context, ImIndirectExpr ptr) {
    ArrayRef ref = ptr.translateToArrayRef(context);
    JimpleType wrapperType = ptr.type().getWrapperType();

    String tempWrapper = context.declareTemp(wrapperType);
    context.getBuilder().addStatement(tempWrapper + " = new " + wrapperType);
    context.getBuilder().addStatement(
        "specialinvoke " + tempWrapper + ".<" + wrapperType + ": void <init>("
            + ptr.type().getArrayType() + ", int)>(" + ref.getArrayExpr() + ", " + ref.getIndexExpr() + ")");

    return new JimpleExpr(tempWrapper);
  }
}
