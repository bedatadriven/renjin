package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * A nullary expression (used by the Goto statement, for example,
 * which as no rhs expression. (Not the same as a constant Null.INSTANCE !)
 */

public class NullExpression implements Expression {

  public static final NullExpression INSTANCE = new NullExpression();
  
  private NullExpression() { }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public Expression replaceVariable(Variable name, Variable newName) {
    return this;
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

}
