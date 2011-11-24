package r.compiler.ir.tac.instructions;

import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.LValue;


public class Assignment implements Statement {
  private Operand rvalue;
  private LValue target;
 
  public Assignment(LValue target, Operand rvalue) {
    this.target = target;
    this.rvalue = rvalue;
  }
 
  public LValue getTarget() {
    return target;
  }
  
  public Operand getRValue() {
    return rvalue;
  }
  
  @Override 
  public String toString() {
    return getTarget() + " := " + rvalue;
  }
  
}
