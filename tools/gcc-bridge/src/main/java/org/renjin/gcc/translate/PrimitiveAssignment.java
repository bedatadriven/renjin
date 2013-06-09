package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImLValue;
import org.renjin.gcc.translate.expr.PrimitiveLValue;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.PrimitiveTypes;

public class PrimitiveAssignment {

  public static void assign(FunctionContext context, ImExpr lhs, ImExpr rhs) {
    if(lhs instanceof PrimitiveLValue && rhs.type() instanceof ImPrimitiveType) {
      ImPrimitiveType lhsType = (ImPrimitiveType) lhs.type();
      JimpleExpr jimpleExpr = rhs.translateToPrimitive(context, lhsType);

      ((PrimitiveLValue) lhs).writePrimitiveAssignment(jimpleExpr);
//    } else if(lhs instanceof ImLValue) {
//      ((ImLValue) lhs).writeAssignment(context, rhs);


    } else {

      throw new UnsupportedOperationException(String.format("Unable to assign %s to %s", rhs, lhs));
    }
  }

  
}
