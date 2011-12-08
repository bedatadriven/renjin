package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.IRLabel;
import r.lang.Context;

public class GotoStatement implements Statement {

  private final IRLabel target;

  public GotoStatement(IRLabel target) {
    this.target = target;
  }

  public IRLabel getTarget() {
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
