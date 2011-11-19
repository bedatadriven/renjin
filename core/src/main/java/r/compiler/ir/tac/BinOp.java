package r.compiler.ir.tac;


/**
 * Refers to a real, JVM-byte code binary operation,
 * not R-level operators (though we hope to transform
 * R-level operator function calls to JVM byte codes 
 * in the course of optimization)
 */
public abstract class BinOp implements Expr {
  
  private final Expr operand1;
  private final Expr operand2;
  
  
  public BinOp(Expr operand1, Expr operand2) {
    super();
    this.operand1 = operand1;
    this.operand2 = operand2;
  }

  public Expr getOperand1() {
    return operand1;
  }

  public Expr getOperand2() {
    return operand2;
  }  
  
  public abstract String getSymbol();

  @Override
  public String toString() {
    return operand1 + " " + getSymbol() + " " + operand2;
  }
  
  
}
