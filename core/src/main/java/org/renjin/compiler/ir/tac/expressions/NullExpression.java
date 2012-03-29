package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


/**
 * A nullary expression (used by the Goto statement, for example,
 * which as no rhs expression. (Not the same as a constant Null.INSTANCE !)
 */

public class NullExpression implements Expression {

  public static final NullExpression INSTANCE = new NullExpression();
  
  private NullExpression() { }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public Expression replaceVariable(Variable name, Variable newName) {
    return this;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    // NOOP
  }

  @Override
  public SEXP getSExpression() {
    throw new UnsupportedOperationException();
  }
}
