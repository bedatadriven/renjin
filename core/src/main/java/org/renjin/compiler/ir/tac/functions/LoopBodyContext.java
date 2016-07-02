package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.sexp.*;

public class LoopBodyContext implements TranslationContext {

  private Environment rho;
  private final SEXP ellipses;

  public LoopBodyContext(Environment rho) {
    this.rho = rho;
    this.ellipses = rho.getVariable(Symbols.ELLIPSES);
  }

  @Override
  public PairList getEllipsesArguments() {
    if(ellipses == Symbol.UNBOUND_VALUE) {
      throw new InvalidSyntaxException("'...' used in incorrect context");
    }
    return (PairList) ellipses;
  }
}
