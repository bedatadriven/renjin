package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SpecialFunction;


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