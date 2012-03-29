package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.BuiltinFunction;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;


public class UnimplementedPrimitive extends BuiltinFunction {

  public UnimplementedPrimitive(String name) {
    super(name);
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call,
      PairList args) {
    throw new EvalException("Sorry! " + getName() + "() is not yet implemented.");
  }
  
  public static SEXP matchAndApply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] arguments) {
    throw new EvalException("Sorry! " + call.getFunction() + "() is not yet implemented.");
  }

}
