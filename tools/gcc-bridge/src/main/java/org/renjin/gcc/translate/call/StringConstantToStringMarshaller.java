package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;

/**
 * Marshalls a String literal to java.lang.String
 *
 */
public class StringConstantToStringMarshaller extends ParamMarshaller {

  @Override
  public JimpleExpr marshall(FunctionContext context, GimpleExpr expr,
      CallParam param) {
    if(expr instanceof GimpleConstant) {
      Object value = ((GimpleConstant) expr).getValue();
      if(value instanceof String && isStringParam(param)) {
        return JimpleExpr.stringLiteral((String) value);
      }
    }
    throw new CannotMarshallException();
  }

  private boolean isStringParam(CallParam param) {
    return param instanceof JvmObjectCallParam &&
        ((JvmObjectCallParam) param).getParamClass().isAssignableFrom(String.class);
    
  }

  
  
}
