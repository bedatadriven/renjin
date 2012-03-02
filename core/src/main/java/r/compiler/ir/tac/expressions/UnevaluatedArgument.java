package r.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import r.lang.Context;
import r.lang.Promise;
import r.lang.SEXP;

/**
 * An unevaluated argument to a dynamic function call. 
 * (it will become a promise at runtime)
 *
 */
public class UnevaluatedArgument implements SimpleExpression {
  
  /**
   * The original expression
   */
  private SEXP exp;

  public UnevaluatedArgument(SEXP exp) {
    this.exp = exp;
  }

  @Override
  public SEXP retrieveValue(Context context, Object[] temps) {
    return exp;
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitPromise(this);
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public SimpleExpression replaceVariable(Variable name, Variable newName) {
    return this;
  }

  @Override
  public String toString() {
    return "UNEVALED[ " + exp + " ]";
  }
}
