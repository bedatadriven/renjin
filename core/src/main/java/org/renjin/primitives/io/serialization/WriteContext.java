package org.renjin.primitives.io.serialization;


import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

/**
 * Provides contextual information needed for serializing
 * environments
 */
public interface WriteContext {
  public boolean isBaseEnvironment(Environment exp);
  boolean isNamespaceEnvironment(Environment exp);
  boolean isBaseNamespaceEnvironment(Environment ns);
  boolean isGlobalEnvironment(Environment env);

  String getNamespaceName(Environment ns);

}
