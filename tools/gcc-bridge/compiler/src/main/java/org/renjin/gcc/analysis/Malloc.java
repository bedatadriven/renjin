package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;

/**
 * Predicates for malloc statements
 */
public class Malloc {
  public static boolean isMalloc(GimpleExpr functionExpr) {
    return isFunctionNamed(functionExpr, "malloc")  ||
           isFunctionNamed(functionExpr, "__builtin_malloc");
  }

  private static boolean isFunctionNamed(GimpleExpr functionExpr, String name) {
    if (functionExpr instanceof GimpleAddressOf) {
      GimpleAddressOf addressOf = (GimpleAddressOf) functionExpr;
      if (addressOf.getValue() instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
        return ref.getName().equals(name);
      }
    }
    return false;
  }
}
