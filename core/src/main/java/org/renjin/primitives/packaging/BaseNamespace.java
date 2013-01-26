package org.renjin.primitives.packaging;

import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class BaseNamespace extends Namespace {

  public BaseNamespace(Environment baseNamespaceEnvironment) {
    super(new BasePackage(), "base", baseNamespaceEnvironment);
  }

  @Override
  public SEXP getExport(Symbol entry) {
    // all symbols in base namespace are exported
    return getEntry(entry);
  }

  
}
