package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.operand.Operand;

public class Return implements Statement {

  private final Operand value;

  public Return(Operand value) {
    super();
    this.value = value;
  }
  
  public Operand getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return "return " + value;
  }
  
}
