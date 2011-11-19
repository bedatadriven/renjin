package r.compiler.ir.tac;


public class Constant implements SimpleExpr {

  private Object value;
  
  public Constant(Object value) {
    this.value = value;
  }
  
  public Object getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return value.toString();
  }
}
