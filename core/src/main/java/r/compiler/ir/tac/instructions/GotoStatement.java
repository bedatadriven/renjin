package r.compiler.ir.tac.instructions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.operand.NullOperand;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Variable;
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

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public Operand getRHS() {
    return NullOperand.INSTANCE;
  }

  @Override
  public Statement withRHS(Operand newRHS) {
    if(newRHS != NullOperand.INSTANCE) {
      throw new IllegalArgumentException();
    }
    return this;
  }
}
