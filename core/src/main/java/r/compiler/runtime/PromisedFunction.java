package r.compiler.runtime;

import r.lang.Context;
import r.lang.Environment;
import r.lang.Null;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.exception.EvalException;

public class PromisedFunction extends Promise {
  
  private String className;

  public PromisedFunction(Context context, Environment environment, String className) {
    super(context, environment, Null.INSTANCE);
    this.className = className;
  }

  @Override
  protected SEXP doEval() {
    try {
      return (SEXP) Class.forName(className).getConstructor(Environment.class)
        .newInstance(environment);
    } catch (Exception e) {
      throw new EvalException("Could not load class '" + className + "'");
    }
  }
}
