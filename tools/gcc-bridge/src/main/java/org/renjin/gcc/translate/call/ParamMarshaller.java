package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;

public abstract class ParamMarshaller {

  public abstract JimpleExpr marshall(FunctionContext context, GimpleExpr expr, CallParam param);
  
}
