package org.renjin.eval;

import org.renjin.invoke.codegen.ArgumentException;
import org.renjin.sexp.Symbol;

public class MissingArgumentException extends ArgumentException {

  public MissingArgumentException(Symbol symbol) {
    super("argument '" + symbol.getPrintName() + "' is missing, with no default");
  }

  public MissingArgumentException(String message) {
    super(message);
  }
}
