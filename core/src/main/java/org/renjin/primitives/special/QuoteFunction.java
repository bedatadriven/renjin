package org.renjin.primitives.special;

import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.SpecialFunction;

public class QuoteFunction extends SpecialFunction {

  public QuoteFunction() {
    super("quote");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call,
      PairList args) {
    return args.getElementAsSEXP(0);
  }
}