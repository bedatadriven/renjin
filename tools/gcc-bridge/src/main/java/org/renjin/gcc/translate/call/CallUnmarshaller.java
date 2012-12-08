package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;

public abstract class CallUnmarshaller {

  public abstract boolean unmarshall(FunctionContext context, GimpleLValue lhs, JimpleType type, JimpleExpr callExpr);
  
}
