package r.compiler.ir;

import r.lang.Context;
import r.lang.Null;
import r.lang.Promise;
import r.lang.SEXP;

public class Thunk extends Promise {

  public Thunk(Context context) {
    super(context, context.getEnvironment(), Null.INSTANCE);
  }

  @Override
  public SEXP getExpression() {
    throw new UnsupportedOperationException();
  }

}
