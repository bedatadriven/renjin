package r.compiler.ir.tac;

public class Label implements Node {
  
  private final int index;
  
  public Label(int index) {
    this.index = index;
  }
  
  public int getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return "L" + index;
  }
}
