package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;


public class PromisedFunction extends Promise {
  
  private String className;

  public PromisedFunction(Context context, Environment environment, String className) {
    super(environment, Null.INSTANCE);
    this.className = className;
  }

  @Override
  protected SEXP doEval(Context context) {
    try {
      return (SEXP) Class.forName(className).getConstructor(Environment.class)
        .newInstance(environment);
    } catch (Exception e) {
      throw new EvalException("Could not load class '" + className + "'");
    }
  }
}
