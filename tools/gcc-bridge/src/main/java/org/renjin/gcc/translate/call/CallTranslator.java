package org.renjin.gcc.translate.call;


import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;

public interface CallTranslator {

  boolean accept(GimpleCall call);

  void writeCall(FunctionContext context, GimpleCall call);
}
