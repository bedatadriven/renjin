package org.renjin.compiler.ir.tac.functions;

import org.renjin.sexp.Symbol;

public class AssignTranslator extends AssignLeftTranslator {

  @Override
  public Symbol getName() {
    return Symbol.get("=");
  }

}
