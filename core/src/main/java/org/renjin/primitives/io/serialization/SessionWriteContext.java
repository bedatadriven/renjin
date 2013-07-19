package org.renjin.primitives.io.serialization;

import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Symbol;


public class SessionWriteContext implements WriteContext {
  private Session session;

  public SessionWriteContext(Session session) {
    this.session = session;
  }

  @Override
  public boolean isBaseEnvironment(Environment exp) {
    return exp == session.getBaseEnvironment();
  }

  @Override
  public boolean isNamespaceEnvironment(Environment environment) {
    return session.getNamespaceRegistry().isNamespaceEnv(environment);
  }

  @Override
  public boolean isBaseNamespaceEnvironment(Environment ns) {
    return ns == session
        .getNamespaceRegistry()
        .getNamespace(Symbol.get("base"))
        .getNamespaceEnvironment();
  }

  @Override
  public boolean isGlobalEnvironment(Environment env) {
    return env == session.getGlobalEnvironment();
  }

  @Override
  public String getNamespaceName(Environment ns) {
    return session.getNamespaceRegistry().getNamespace(ns).getName();
  }
}
