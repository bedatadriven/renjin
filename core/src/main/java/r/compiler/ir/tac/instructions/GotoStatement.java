package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.Label;
import r.lang.Context;

public class GotoStatement implements Statement {

  private final Label target;

  public GotoStatement(Label target) {
    this.target = target;
  }

  public Label getTarget() {
    return target;
  }

  @Override
  public String toString() {
    return "goto " + target;
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    return target;
  } 
}
