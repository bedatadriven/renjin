package r.compiler.ir.tac.instructions;

import java.util.Arrays;
import java.util.Collections;

import r.compiler.ir.tac.IRLabel;
import r.lang.Context;

public class GotoStatement implements Statement, BasicBlockEndingStatement {

  private final IRLabel target;

  public GotoStatement(IRLabel target) {
    this.target = target;
  }

  public IRLabel getTarget() {
    return target;
  }
  
  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Arrays.asList(target);
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
