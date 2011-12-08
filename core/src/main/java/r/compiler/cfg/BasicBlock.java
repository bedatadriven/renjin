package r.compiler.cfg;

import r.compiler.ir.tac.IRBlock;

public class BasicBlock {
  private final IRBlock parent;
  
  private int startIndex;
  private int endIndex;
  
  public BasicBlock(IRBlock parent, int startIndex, int endIndex) {
    super();
    this.parent = parent;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }
  
  public int size() {
    return endIndex - startIndex;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(int i=startIndex;i<endIndex;++i) {
      parent.appendLineTo(sb, i);
    }
    return sb.toString();
  }
  

}
