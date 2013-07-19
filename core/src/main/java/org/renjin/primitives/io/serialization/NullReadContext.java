package org.renjin.primitives.io.serialization;

import org.renjin.sexp.*;

/**
 * A "null" read context which will deserialize references
 * to special environments like base and global with the
 * empty environment.
 *
 * <p>This is useful if you just want to deserialize R data
 * outside of an R {@link org.renjin.eval.Session}</p>
 */
public class NullReadContext implements ReadContext {

  @Override
  public Environment getBaseEnvironment() {
    return Environment.EMPTY;
  }

  @Override
  public Promise createPromise(SEXP expr, Environment environment) {
    return Promise.repromise(Null.INSTANCE);
  }

  @Override
  public Environment findNamespace(Symbol symbol) {
    return Environment.EMPTY;
  }

  @Override
  public Environment getBaseNamespaceEnvironment() {
    return Environment.EMPTY;
  }

  @Override
  public Environment getGlobalEnvironment() {
    return Environment.EMPTY;
  }
}
