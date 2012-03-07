package r.compiler.runtime;

import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.Promise;
import r.lang.SEXP;

public class CompiledThunkExample extends Promise {

  public static SEXP s0;
  
  static {
    
    s0 = new FunctionCall(Null.INSTANCE, Null.INSTANCE);
    
  }
  
  public CompiledThunkExample(Context context, Environment rho) {
    super(context, rho, createBody() );
  }

  private static SEXP createBody() {
    return s0;
  }

  @Override
  protected SEXP doEval() {
    return Null.INSTANCE;
  }

  public static SEXP doEval(Context context, Environment rho) {
    return Null.INSTANCE;
  }
  
}
