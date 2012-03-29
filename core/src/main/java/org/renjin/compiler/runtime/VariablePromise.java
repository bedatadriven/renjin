package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


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
    if(value instanceof Promise) {
      return value.force();
    } else {
      return value;
    }
  }
  
}
