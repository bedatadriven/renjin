package r.compiler.ir.tac.functions;

import r.compiler.ir.tac.Label;

public class LoopContext implements TranslationContext {

  private final Label startLabel;
  private final Label exitLabel;
  
  public LoopContext(Label startLabel, Label exitLabel) {
    super();
    this.startLabel = startLabel;
    this.exitLabel = exitLabel;
  }

  public Label getStartLabel() {
    return startLabel;
  }

  public Label getExitLabel() {
    return exitLabel;
  }
  
}
