package r.compiler.ir.tac;

public class AddOp extends BinOp {

  public AddOp(Expr operand1, Expr operand2) {
    super(operand1, operand2);
  }

  @Override
  public String getSymbol() {
    return "+";
  }

}
