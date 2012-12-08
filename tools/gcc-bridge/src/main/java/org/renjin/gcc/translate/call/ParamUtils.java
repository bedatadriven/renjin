package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;

public class ParamUtils {

  public static String isStringConstant(GimpleExpr expr) {
    if(expr instanceof GimpleConstant && ((GimpleConstant) expr).getValue() instanceof String) {
      return (String)(((GimpleConstant) expr).getValue()) ;
    }
    return null;
  }
  
}
