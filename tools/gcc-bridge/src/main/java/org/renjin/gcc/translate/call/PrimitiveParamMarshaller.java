package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;

public class PrimitiveParamMarshaller extends ParamMarshaller {

  @Override
  public JimpleExpr marshall(FunctionContext context, GimpleExpr expr, CallParam param) {
    if(param instanceof PrimitiveCallParam) {
      PrimitiveCallParam primitiveParam = (PrimitiveCallParam) param;
      
      return context.asNumericExpr(expr, ((PrimitiveCallParam) param).getType());
    } else {
      throw new CannotMarshallException();
    }
  }

  
  
}
