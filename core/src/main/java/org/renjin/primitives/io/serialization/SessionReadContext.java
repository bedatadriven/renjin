package org.renjin.primitives.io.serialization;

import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class SessionReadContext implements ReadContext {
  private Session session;

  public SessionReadContext(Session session) {
    this.session = session;
  }

  @Override
  public Environment getBaseEnvironment() {
    return session.getBaseEnvironment();
  }

  @Override
  public Promise createPromise(SEXP expr, Environment env) {
    return Promise.repromise(env, expr);
  }

  @Override
  public Environment findNamespace(Symbol symbol) {
    return session.getNamespaceRegistry().getNamespace(symbol).getNamespaceEnvironment();
  }

  @Override
  public Environment getBaseNamespaceEnvironment() {
    return session.getBaseNamespaceEnv();
  }

  @Override
  public Environment getGlobalEnvironment() {
    return session.getGlobalEnvironment();
  }
}
