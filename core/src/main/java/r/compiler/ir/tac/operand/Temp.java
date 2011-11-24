package r.compiler.ir.tac.operand;

public class Temp implements LValue, SimpleExpr {
  private final int index;
  
  public Temp(int index) {
    this.index = index;
  }
  
  public int getIndex() {
    return index;
  }
  
  @Override 
  public String toString() {
    return "_t" + index;
  }
}
