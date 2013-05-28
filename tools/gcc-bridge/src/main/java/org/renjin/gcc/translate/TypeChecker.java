package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;

public class TypeChecker {

  public static void assertSameType(ImExpr expr, ImExpr... otherExprs) {
    
    for(ImExpr other : otherExprs) {
      if(!expr.type().equals(other.type())) {
        throw new IllegalArgumentException(String.format("Types do not match: %s:%s <> %s:%s",
            expr.toString(), expr.type().toString(),
            other.toString(), other.type().toString()));
      }
    }
  }

  public static ImPrimitiveType assertSamePrimitiveType(ImExpr expr, ImExpr... otherExprs) {
    assertSameType(expr, otherExprs);
    return (ImPrimitiveType) expr.type();
  }
  
  public static boolean isDouble(ImType type) {
    return type == ImPrimitiveType.DOUBLE;
  }
  
  public static boolean isInt(ImType type) {
    return type == ImPrimitiveType.INT;
  }

  public static boolean isLong(ImType type) {
    return type == ImPrimitiveType.LONG;
  }

}
