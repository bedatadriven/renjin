package org.renjin.primitives.special;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

public class CallFunction extends SpecialFunction {

  public CallFunction() {
    super("call");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    if (call.length() < 1) {
      throw new EvalException("first argument must be character string");
    }
    SEXP name = context.evaluate(call.getArgument(0), rho);
    if (!(name instanceof StringVector) || name.length() != 1) {
      throw new EvalException("first argument must be character string");
    }

    Symbol function = Symbol.get(((StringVector) name).getElementAsString(0));
    PairList callArguments = ((PairList.Node) call.getArguments()).getNext();

    return new FunctionCall(function, callArguments);
  }
}
