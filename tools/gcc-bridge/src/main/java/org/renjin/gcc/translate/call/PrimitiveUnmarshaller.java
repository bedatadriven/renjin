package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.var.PrimitiveVar;
import org.renjin.gcc.translate.var.Variable;

public class PrimitiveUnmarshaller extends CallUnmarshaller {
  
  public boolean unmarshall(FunctionContext context, GimpleLValue lhs, JimpleType type, JimpleExpr callExpr) {
    if(type.isPrimitive()) {
      PrimitiveVar var = isPrimitiveVar(context, lhs);
      if(var != null) {
        var.assign(callExpr);
        return true;
      }
    }
    return false;
  }
  
  private PrimitiveVar isPrimitiveVar(FunctionContext context, GimpleLValue lhs) {
    if(lhs instanceof GimpleVar) {
      Variable var = context.lookupVar(lhs);
      if(var instanceof PrimitiveVar) {
        return (PrimitiveVar) var;
      }
    }
    return null;
   }
}
