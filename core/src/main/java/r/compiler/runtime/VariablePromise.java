package r.compiler.runtime;

import r.lang.Context;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class VariablePromise extends Promise {

  public VariablePromise(Context context, String name) {
    super(context, context.getEnvironment(), Symbol.get(name));
  }

  @Override
  protected SEXP doEval() {
    SEXP value = environment.findVariable((Symbol)expression);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("object '" + expression + "' not found");
    }
    return value;
  }
  
}
