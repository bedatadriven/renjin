package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;


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
