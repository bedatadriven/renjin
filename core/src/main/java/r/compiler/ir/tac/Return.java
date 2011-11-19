package r.compiler.ir.tac;

public class Return implements Statement {

  private final Expr value;

  public Return(Expr value) {
    super();
    this.value = value;
  }
  
  public Expr getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return "return " + value;
  }
  
}
