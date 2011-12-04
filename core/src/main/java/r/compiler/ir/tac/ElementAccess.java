package r.compiler.ir.tac;

import r.compiler.ir.tac.operand.Operand;
import r.lang.Context;
import r.lang.Vector;


/**
 * Extracts a single element from a vector.
 */
public class ElementAccess implements Operand {

  private final Operand vector;
  private final Operand index;
  
  public ElementAccess(Operand vector, Operand index) {
    super();
    this.vector = vector;
    this.index = index;
  }

  public Operand getVector() {
    return vector;
  }

  /**
   * @return the value holding the zero-based index of the
   * element to extract
   */
  public Operand getIndex() {
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
 
}
