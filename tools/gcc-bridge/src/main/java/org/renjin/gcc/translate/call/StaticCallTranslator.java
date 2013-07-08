package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.marshall.Marshallers;


public class StaticCallTranslator implements CallTranslator {

  public static final StaticCallTranslator INSTANCE = new StaticCallTranslator();

  private StaticCallTranslator() {

  }
  
  @Override
  public boolean accept(GimpleCall call) {
    GimpleExpr functionExpr = call.getFunction();
    return functionExpr instanceof GimpleAddressOf &&
        ((GimpleAddressOf) functionExpr).getValue() instanceof GimpleFunctionRef;
  }
  
  public void writeCall(FunctionContext context, GimpleCall call) {
    MethodRef method = context.resolveMethod(call);
    StringBuilder callExpr = new StringBuilder();
    callExpr
        .append("staticinvoke")
        .append(method.signature())
        .append(Marshallers.marshallParamList(context, call, method.getParams()));

    Marshallers.writeCall(context, call, callExpr.toString(), method.getReturnType());
  }
}
