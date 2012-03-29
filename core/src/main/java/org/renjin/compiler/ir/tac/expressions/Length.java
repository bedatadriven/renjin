package org.renjin.compiler.ir.tac.expressions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


/**
 * The length of an expression.
 * 
 * <p>This is a bit annoying to add this to the set of expressions,
 * but we need it to translate for expressions, because the length
 * primitive is generic, but the for loop always uses the actual length of the
 * vector. (is this another sign we need to push 'fors' down into the IR level?)
 */
public class Length implements SimpleExpression {

  private Expression vector;
  
  public Length(Expression vector) {
    super();
    this.vector = vector;
  }

  public Expression getVector() {
    return vector;
  }
  
  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    SEXP exp = (SEXP) vector.retrieveValue(context, temps);
    return exp.length();
  }

  @Override
  public Set<Variable> variables() {
    return vector.variables();
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitLength(this);
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.singletonList(vector);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SimpleExpression replaceVariable(Variable name, Variable newName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "length(" + vector + ")";
  }

  @Override
  public SEXP getSExpression() {
    throw new UnsupportedOperationException();
  }
}
