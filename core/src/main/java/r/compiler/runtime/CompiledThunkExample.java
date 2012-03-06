package r.compiler.runtime;

import r.lang.Context;
import r.lang.Environment;
import r.lang.Null;
import r.lang.Promise;
import r.lang.SEXP;

public class CompiledThunkExample extends Promise {

  public CompiledThunkExample(Context context, Environment rho) {
    super(context, rho, createBody() );
  }

  private static SEXP createBody() {
    return Null.INSTANCE;
  }

  @Override
  protected SEXP doEval() {
    return Null.INSTANCE;
  }

  public static SEXP doEval(Context context, Environment rho) {
    return Null.INSTANCE;
  }
  
}
