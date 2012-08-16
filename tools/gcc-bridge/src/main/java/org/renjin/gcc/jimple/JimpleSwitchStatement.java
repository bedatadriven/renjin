package org.renjin.gcc.jimple;


import com.google.common.collect.Lists;

import java.util.List;

public class JimpleSwitchStatement extends JimpleBodyElement {

  public static final int DEFAULT = Integer.MIN_VALUE;

  public static class Branch {
    private int value;
    private String target;

    public Branch(int value, String target) {
      this.value = value;
      this.target = target;
    }

    public boolean isDefault() {
      return value == DEFAULT;
    }
  }

  private String switchExpr;
  private List<Branch> branches = Lists.newArrayList();

  public JimpleSwitchStatement(String switchExpr) {
    this.switchExpr = switchExpr;
  }

  public void addBranch(int value, String target) {
    this.branches.add(new Branch(value, target));
  }

  @Override
  public void write(JimpleWriter w) {
    w.println("lookupswitch(" + switchExpr + ")");
    w.startBlock();
    for(Branch branch : branches) {
      if(branch.isDefault()) {
        w.println("default: goto " + branch.target + ";");
      } else {
        w.println("case " + branch.value + ": goto " + branch.target + ";") ;
      }
    }
    w.closeBlockWithSemicolon();
  }

  @Override
  public String toString() {
    return "switch(" + switchExpr + ")";
  }
}
