package org.renjin.gcc.gimple;


import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.util.List;

public class GimpleSwitch extends GimpleIns {

  public static final int DEFAULT = Integer.MIN_VALUE;

  public static class Branch {
    private final int value;
    private final GimpleLabel label;

    public Branch(int value, GimpleLabel label) {
      this.value = value;
      this.label = label;
    }

    public int getValue() {
      return value;
    }

    public GimpleLabel getLabel() {
      return label;
    }
  }

  private final GimpleExpr expr;
  private final List<Branch> branches;

  public GimpleSwitch(GimpleExpr expr, List<Branch> branches) {
    this.expr = expr;
    this.branches = branches;
  }

  public List<Branch> getBranches() {
    return branches;
  }

  public GimpleExpr getExpr() {
    return expr;
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitSwitch(this);
  }
}
