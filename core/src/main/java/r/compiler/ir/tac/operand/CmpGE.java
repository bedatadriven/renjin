package r.compiler.ir.tac.operand;

public class CmpGE implements SimpleExpr {
  private final Operand op1;
  private final Operand op2;
  
  public CmpGE(Operand op1, Operand op2) {
    super();
    this.op1 = op1;
    this.op2 = op2;
  }

  @Override
  public String toString() {
    return op1 + " >= " + op2;
  }
}
