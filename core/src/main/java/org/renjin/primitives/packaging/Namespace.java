package org.renjin.primitives.packaging;

import org.renjin.eval.EvalException;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class Namespace {
  
  private Environment namespaceEnvironment;
  

  public Namespace(Environment namespaceEnvironment) {
    this.namespaceEnvironment = namespaceEnvironment;
  }

  public SEXP getEntry(Symbol entry) {
    throw new EvalException("unimplemented");
  }

  public SEXP getExport(Symbol entry) {
    throw new EvalException("unimplemented");
  }

  public Environment getNamespaceEnvironment() {
    return this.namespaceEnvironment;
  }

}
