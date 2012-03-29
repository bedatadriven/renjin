package org.renjin.compiler.ir.tac.functions;

import org.renjin.compiler.ir.tac.IRLabel;

public class LoopContext implements TranslationContext {

  private final IRLabel startLabel;
  private final IRLabel exitLabel;
  
  public LoopContext(IRLabel startLabel, IRLabel exitLabel) {
    super();
    this.startLabel = startLabel;
    this.exitLabel = exitLabel;
  }

  public IRLabel getStartLabel() {
    return startLabel;
  }

  public IRLabel getExitLabel() {
    return exitLabel;
  }
  
}
