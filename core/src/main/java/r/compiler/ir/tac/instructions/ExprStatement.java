package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.operand.Operand;
import r.lang.Context;

/**
 * Statement that is evaluated for side-effects
 */
public class ExprStatement implements Statement {

  private Operand operand;
  
  public ExprStatement(Operand operand) {
    super();
    this.operand = operand;
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return operand.toString();
  }
  
  

}
