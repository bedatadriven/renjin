package org.renjin.compiler.ir.tac.expressions;


import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Element access in the form x$name
 */
public class NamedElementAccess implements Expression {

  private Expression listExpression;
  private String memberName;

  public NamedElementAccess(Expression expression, String memberName) {
    this.listExpression = expression;
    this.memberName = memberName;
  }

  @Override
  public Set<Variable> variables() {
    return listExpression.variables();
  }

  @Override
  public Expression replaceVariable(Variable variable, Variable newVariable) {
    return new NamedElementAccess(listExpression.replaceVariable(variable, newVariable), memberName);
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.singletonList(listExpression);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    switch (childIndex) {
      case 0:
        this.listExpression = child;
        break;
      default:
        throw new IllegalArgumentException("index: " + childIndex);
    }
  }
}
