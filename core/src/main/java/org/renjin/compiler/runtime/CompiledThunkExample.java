package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.sexp.*;


public class CompiledThunkExample extends Promise {

  public static SEXP s0;
  
  static {
    
    s0 = new FunctionCall(Null.INSTANCE, Null.INSTANCE);
    
  }
  
  public CompiledThunkExample(Context context, Environment rho) {
    super(rho, createBody() );
  }

  private static SEXP createBody() {
    return s0;
  }

  @Override
  protected SEXP doEval(Context context) {
    return Null.INSTANCE;
  }

  public static SEXP doEval(Context context, Environment rho) {
    return Null.INSTANCE;
  }
  
}
