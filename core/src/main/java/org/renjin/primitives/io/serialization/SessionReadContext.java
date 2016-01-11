package org.renjin.primitives.io.serialization;

import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class SessionReadContext implements ReadContext {
  private Context context;

  /**
   * @deprecated Use {@link SessionReadContext#SessionReadContext(Context)}  to ensure that 
   * any code evaluated during namespace loading is evaluated within the correct context.
   */
  @Deprecated
  public SessionReadContext(Session session) {
    this(session.getTopLevelContext());
  }
  
  public SessionReadContext(Context context) {
    this.context = context;
  }

  @Override
  public Environment getBaseEnvironment() {
    return context.getBaseEnvironment();
  }

  @Override
  public Promise createPromise(SEXP expr, Environment env) {
    return Promise.repromise(env, expr);
  }

  @Override
  public Environment findNamespace(Symbol symbol) {
    return context.getNamespaceRegistry().getNamespace(context, symbol).getNamespaceEnvironment();
  }

  @Override
  public Environment getBaseNamespaceEnvironment() {
    return context.getSession().getBaseNamespaceEnv();
  }

  @Override
  public Environment getGlobalEnvironment() {
    return context.getGlobalEnvironment();
  }
}
