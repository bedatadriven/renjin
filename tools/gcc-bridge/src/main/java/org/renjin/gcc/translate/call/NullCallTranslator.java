package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.translate.FunctionContext;

public class NullCallTranslator extends NamedCallTranslator {

  public NullCallTranslator(String functionName) {
    super(functionName);
  }

  @Override
  public void writeCall(FunctionContext context, GimpleCall call) {
    // do nothing
  }
}
