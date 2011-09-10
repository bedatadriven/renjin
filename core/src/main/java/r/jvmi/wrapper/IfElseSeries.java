package r.jvmi.wrapper;

public class IfElseSeries {
  private WrapperSourceWriter s;
  private boolean first = true;
  private int count;
  private int index;
  
  public IfElseSeries(WrapperSourceWriter s, int count) {
    this.s = s;
    this.count = count;
    this.index = 0;
  }

  public void elseIf(String condition) {
    if(count > 1) {
      if(index+1 == count) {
        s.writeElse();
      } else if(first) {
        s.writeBeginIf(condition);
        first = false;
      } else {
        s.writeBeginIfElse(condition);
      }
    }
    index++;
  }
  
  public void finish() {
    if(count > 1) {
      s.writeCloseBlock();
    }
  }

}
