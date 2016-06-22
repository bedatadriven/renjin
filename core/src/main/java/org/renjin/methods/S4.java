package org.renjin.methods;

import org.renjin.sexp.Symbol;

/**
 * Methods for working with S4 Metadata
 */
public class S4 {

  public static Symbol classNameMetadata(String className) {
    return Symbol.get(".__C__" + className);
  }
}
