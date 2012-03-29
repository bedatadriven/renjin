package org.renjin.compiler.ir.tac.statements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.eval.Context;


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

  @Override
  public List<Expression> getChildren() {
    return Arrays.asList(operand);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      operand = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitExprStatement(this);
  }
}
