package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.expr.ArrayRef;
import org.renjin.gcc.translate.expr.ImIndirectExpr;
import org.renjin.gcc.translate.type.ImPrimitivePtrType;
import org.renjin.gcc.translate.type.PrimitiveTypes;

public class PtrWrapperUtils {

  public static JimpleExpr wrapPointer(FunctionContext context, ImIndirectExpr ptr) {
    ArrayRef ref = ptr.translateToArrayRef(context);
    ImPrimitivePtrType ptrType = (ImPrimitivePtrType) ptr.type();
    JimpleType wrapperType = ptrType.getWrapperJimpleType();

    String tempWrapper = context.declareTemp(wrapperType);
    context.getBuilder().addStatement(tempWrapper + " = new " + wrapperType);
    context.getBuilder().addStatement(
        "specialinvoke " + tempWrapper + ".<" + wrapperType + ": void <init>("
            + ptrType.getArrayType() + ", int)>(" + ref.getArrayExpr() + ", " + ref.getIndexExpr() + ")");

    return new JimpleExpr(tempWrapper);
  }
}
