package r.compiler.runtime;

import r.lang.BuiltinFunction;
import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.exception.EvalException;

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
