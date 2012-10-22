package org.renjin.compiler.ir;

import org.renjin.eval.Context;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;


public class Thunk extends Promise {

  public Thunk(Context context) {
    super(context.getEnvironment(), Null.INSTANCE);
  }

  @Override
  public SEXP getExpression() {
    throw new UnsupportedOperationException();
  }

}
