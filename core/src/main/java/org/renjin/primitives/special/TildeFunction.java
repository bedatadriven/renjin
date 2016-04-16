package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.sexp.*;

public class TildeFunction extends SpecialFunction {

  public TildeFunction() {
    super("~");
  }

  @Override
  public FunctionCall apply(Context context, Environment rho, FunctionCall call,
                            PairList args) {

    return new FunctionCall(call.getFunction(), call.getArguments(),
        AttributeMap.builder()
            .setClass("formula")
            .set(Symbols.DOT_ENVIRONMENT, rho)
            .build());
  }
}
