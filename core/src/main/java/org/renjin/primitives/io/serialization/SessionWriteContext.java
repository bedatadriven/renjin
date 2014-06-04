package org.renjin.primitives.io.serialization;

import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.primitives.packaging.FqPackageName;
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
    // we want to use fully qualified names as much as possible throughout Renjin, but
    // also maintain compatibility with GNU R as much as possible.

    // here we will write out only the local name for the "core" packages such as base, stats, etc
    // but use the fully qualified name (e.g. 'com.acme.xy:foobar') otherwise, which will
    // fail to load in GNU R.

    // if we get to the point of needing to exchange ASTs between Renjin and R then we can always
    // provide an alternate implementation of the WriteContext interface

    FqPackageName packageName = session.getNamespaceRegistry().getNamespace(ns).getFullyQualifiedName();
    if(packageName.getGroupId().equals(FqPackageName.CORE_GROUP_ID)) {
      return packageName.getPackageName();

    } else {
      return packageName.toString(':');
    }
  }
}
