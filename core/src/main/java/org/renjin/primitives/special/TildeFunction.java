package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SpecialFunction;
import org.renjin.sexp.Symbols;

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
