package org.renjin.gcc.gimple.ins;

import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.GimpleLabel;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.util.List;

public class GimpleSwitch extends GimpleIns {

  public static class Case {
    private int low;
    private int high;

    private int basicBlockIndex;

    public Case() {

    }

    public int getLow() {
      return low;
    }

    public void setLow(int low) {
      this.low = low;
    }

    public int getHigh() {
      return high;
    }

    public void setHigh(int high) {
      this.high = high;
    }

    public int getBasicBlockIndex() {
      return basicBlockIndex;
    }

    public void setBasicBlockIndex(int basicBlockIndex) {
      this.basicBlockIndex = basicBlockIndex;
    }
  }

  private GimpleExpr value;
  private List<Case> cases = Lists.newArrayList();
  private Case defaultCase;

  public GimpleSwitch() {
  }

  public List<Case> getCases() {
    return cases;
  }

  public Case getDefaultCase() {
    return defaultCase;
  }

  public void setDefaultCase(Case defaultCase) {
    this.defaultCase = defaultCase;
  }

  public GimpleExpr getValue() {
    return value;
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitSwitch(this);
  }
}
