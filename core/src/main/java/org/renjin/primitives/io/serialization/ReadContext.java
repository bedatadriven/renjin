package org.renjin.primitives.io.serialization;

import org.renjin.sexp.Environment;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


/**
 * Provides contextual information required to deserialize
 * an R object graph
 */
public interface ReadContext {

  Environment getBaseEnvironment();

  Promise createPromise(SEXP expr, Environment environment);

  Environment findNamespace(Symbol symbol);

  Environment getBaseNamespaceEnvironment();

  Environment getGlobalEnvironment();
}
