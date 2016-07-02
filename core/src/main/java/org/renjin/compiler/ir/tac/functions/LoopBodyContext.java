package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.invoke.reflection.converters.RuntimeConverter;
import org.renjin.sexp.*;

public class LoopBodyContext implements TranslationContext {

  private RuntimeState runtimeState;

  public LoopBodyContext(RuntimeState runtimeState) {
    this.runtimeState = runtimeState;
  }

  @Override
  public PairList getEllipsesArguments() {
    return runtimeState.getEllipsesVariable();
  }
}
