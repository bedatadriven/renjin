package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.SymbolRef;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.marshall.Marshallers;
import org.renjin.gcc.translate.type.ImFunctionPtrType;
import org.renjin.gcc.translate.type.ImFunctionType;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.var.FunPtrVar;


public class FunPtrCallTranslator implements CallTranslator {

  public static final FunPtrCallTranslator INSTANCE = new FunPtrCallTranslator();

  private FunPtrCallTranslator() {

  }

  public boolean accept(GimpleCall call) {
    return call.getFunction() instanceof SymbolRef;
  }

  @Override
  public void writeCall(FunctionContext context, GimpleCall call) {
    
    ImFunctionType functionType = getFunctionType(context, call);
    FunPtrVar var = getFunPtrVar(context, call);

    StringBuilder callExpr = new StringBuilder();
    callExpr.append("interfaceinvoke ")
        .append(var.getJimpleVariable())
        .append(".")
        .append(functionType.jimpleSignature())
        .append(Marshallers.marshallParamList(context, call, functionType.getParams()));

    Marshallers.writeCall(context, call, callExpr.toString(), functionType.getReturnType());
  }


  private FunPtrVar getFunPtrVar(FunctionContext context, GimpleCall call) {
    ImExpr var = context.lookupVar(call.getFunction());
    if (!(var instanceof FunPtrVar)) {
      throw new UnsupportedOperationException("Function value must be a FunPtrVar, got: " + var);
    }
    return (FunPtrVar) var;
  }

  private ImFunctionType getFunctionType(FunctionContext context, GimpleCall call) {
    ImType type = context.resolveExpr(call.getFunction()).type();
    if (!(type instanceof ImFunctionPtrType)) {
      throw new UnsupportedOperationException("Function value must be of type ImFunctionPtrType, got: " + type);
    }
    return ((ImFunctionPtrType) type).baseType();
  }


}
