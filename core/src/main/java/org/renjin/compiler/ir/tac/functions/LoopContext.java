package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.sexp.PairList;

public class LoopContext implements TranslationContext {

  private TranslationContext parentContext;
  private final IRLabel startLabel;
  private final IRLabel exitLabel;
  
  public LoopContext(TranslationContext parentContext, IRLabel startLabel, IRLabel exitLabel) {
    super();
    this.parentContext = parentContext;
    this.startLabel = startLabel;
    this.exitLabel = exitLabel;
  }

  public IRLabel getStartLabel() {
    return startLabel;
  }

  public IRLabel getExitLabel() {
    return exitLabel;
  }

  @Override
  public PairList getEllipsesArguments() {
    return parentContext.getEllipsesArguments();
  }
}
