package org.renjin.compiler.ir.tac.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import com.google.common.collect.Sets;



/**
 * Extracts a single element from a vector.
 */
public class ElementAccess implements Expression {

  private Expression vector;
  private Expression index;
  
  public ElementAccess(Expression vector, Expression index) {
    super();
    this.vector = vector;
    this.index = index;
  }

  public Expression getVector() {
    return vector;
  }

  /**
   * @return the value holding the zero-based index of the
   * element to extract
   */
  public Expression getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return vector + "[" + index + "]";
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    Vector vectorValue = (Vector) vector.retrieveValue(context, temps);
    Integer indexValue = (Integer)index.retrieveValue(context, temps);
    return vectorValue.getElementAsSEXP(indexValue);
  }

  @Override
  public Set<Variable> variables() {
    return Sets.union(vector.variables(), index.variables());
  }

  @Override
  public ElementAccess replaceVariable(Variable name, Variable newName) {
    return new ElementAccess(
        vector.replaceVariable(name, newName), 
        index.replaceVariable(name, newName));
  }

  @Override
  public List<Expression> getChildren() {
    return Arrays.asList(vector, index);
  }

  @Override
  public void setChild(int i, Expression expr) {
    if(i==0) {
      vector = expr;
    } else if(i==1) {
      index = expr;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitElementAccess(this);
  }

  @Override
  public SEXP getSExpression() {
    throw new UnsupportedOperationException();
  } 
}