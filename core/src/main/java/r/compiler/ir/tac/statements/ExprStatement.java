package r.compiler.ir.tac.statements;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.Variable;
import r.lang.Context;

/**
 * Statement that is evaluated for side-effects
 */
public class ExprStatement implements Statement {

  private Expression operand;
  
  public ExprStatement(Expression operand) {
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
  public Expression getRHS() {
    return operand;
  }

  @Override
  public String toString() {
    return operand.toString();
  }

  @Override
  public Set<Variable> variables() {
    return operand.variables();
  }

  @Override
  public Statement withRHS(Expression newRHS) {
    return new ExprStatement(newRHS);
  }
}
