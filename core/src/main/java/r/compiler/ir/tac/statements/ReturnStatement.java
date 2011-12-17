package r.compiler.ir.tac.statements;

import java.util.Collections;
import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.Variable;
import r.lang.Context;

public class ReturnStatement implements Statement {

  private final Expression value;

  public ReturnStatement(Expression value) {
    super();
    this.value = value;
  }
  
  public Expression getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return "return " + value;
  }

  @Override
  public Object interpret(Context context, Object[] temps) {
    return value.retrieveValue(context, temps);
  }
  
  @Override
  public Expression getRHS() {
    return value;
  }
  
  @Override
  public ReturnStatement withRHS(Expression newRHS) {
    return new ReturnStatement(newRHS);
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  @Override
  public Set<Variable> variables() {
    return value.variables();
  }
}
