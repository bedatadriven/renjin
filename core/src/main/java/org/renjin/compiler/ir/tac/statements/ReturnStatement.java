package org.renjin.compiler.ir.tac.statements;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.eval.Context;


public class ReturnStatement implements Statement {

  private Expression value;

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

  @Override
  public List<Expression> getChildren() {
    return Collections.singletonList(value);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      value = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitReturn(this);
  }
}
