package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.ins.GimpleCall;

public abstract class NamedCallTranslator implements CallTranslator {
  protected final String functionName;

  public NamedCallTranslator(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public boolean accept(GimpleCall call) {
    GimpleExpr functionExpr = call.getFunction();
    if(!(functionExpr instanceof GimpleAddressOf)) {
      return false;
    }
    GimpleExpr value = ((GimpleAddressOf) functionExpr).getValue();
    if (!(value instanceof GimpleFunctionRef)) {
      return false;
    }
    return ((GimpleFunctionRef) value).getName().equals(functionName);

  }
}
