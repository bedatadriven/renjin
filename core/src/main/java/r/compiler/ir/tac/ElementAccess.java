package r.compiler.ir.tac;


/**
 * Extracts a single element from a vector.
 */
public class ElementAccess implements Expr {

  private final Expr vector;
  private final Expr index;
  
  public ElementAccess(Expr vector, Expr index) {
    super();
    this.vector = vector;
    this.index = index;
  }

  public Expr getVector() {
    return vector;
  }

  /**
   * @return the value holding the zero-based index of the
   * element to extract
   */
  public Expr getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return vector + "[" + index + "]";
  }
 
}
