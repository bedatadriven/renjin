package r.compiler.ir.tac.expressions;

import java.util.Set;

import com.google.common.collect.Sets;

import r.lang.Context;
import r.lang.Vector;


/**
 * Extracts a single element from a vector.
 */
public class ElementAccess implements Expression {

  private final Expression vector;
  private final Expression index;
  
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
}