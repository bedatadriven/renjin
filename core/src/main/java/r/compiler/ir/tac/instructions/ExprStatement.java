package r.compiler.ir.tac.instructions;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.Variable;
import r.lang.Context;

/**
 * Statement that is evaluated for side-effects
 */
public class ExprStatement implements Statement {

  private Operand operand;
  
  public ExprStatement(Operand operand) {
    super();
    this.operand = operand;
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    // execute and discard result
    operand.retrieveValue(context, temp);
    return null;
  }
  
  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }
  
  @Override
  public String toString() {
    return operand.toString();
  }

  @Override
  public Set<Variable> variables() {
    return operand.variables();
  }
}
