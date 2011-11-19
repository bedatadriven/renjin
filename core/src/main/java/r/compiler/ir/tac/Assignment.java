package r.compiler.ir.tac;


public class Assignment implements Statement {
  private Expr rvalue;
  private LValue target;
 
  public Assignment(LValue target, Expr rvalue) {
    this.target = target;
    this.rvalue = rvalue;
  }
 
  public LValue getTarget() {
    return target;
  }
  
  public Expr getRValue() {
    return rvalue;
  }
  
  @Override 
  public String toString() {
    return getTarget() + " := " + rvalue;
  }
  
}
