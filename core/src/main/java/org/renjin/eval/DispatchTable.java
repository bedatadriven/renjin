package org.renjin.eval;

import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Collection;

public interface DispatchTable {
  SEXP get(Symbol symbol);

  Collection<Symbol> getEnvironmentSymbols();
}
