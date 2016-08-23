package org.renjin.gcc.gimple.statement;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.GimpleVisitor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GimpleSwitch extends GimpleStatement {

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

    public String toString(){
      return String.format("[ %d, %d]: %d",this.low,this.high,this.basicBlockIndex);
    }

    public int getRange() {
      Preconditions.checkState(low <= high);
      return high - low + 1;
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

  /**
   * 
   * Finds the total number of distinct cases. When a case has a range (low != high), then
   * each value in the range is considered a distinct case.
   * 
   * @return the total count of cases. 
   */
  public int getCaseCount() {
    int count = 0;
    for (Case aCase : cases) {
      count += aCase.getRange();
    }
    return count;
  }
  
  public Case getDefaultCase() {
    return defaultCase;
  }

  public void setDefaultCase(Case defaultCase) {
    this.defaultCase = defaultCase;
  }

  public void setValue(GimpleExpr value) {
    this.value = value;
  }

  @Override
  public List<GimpleExpr> getOperands() {
    return Collections.singletonList(value);
  }
  
  @Override
  protected void findUses(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    value.findOrDescend(predicate, results);
  }

  @Override
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    if(predicate.apply(value)) {
      value = newExpr;
    } else {
      value.replaceAll(predicate, newExpr);
    }
  }

  @Override
  public void accept(GimpleExprVisitor visitor) {
    value.accept(visitor);
  }

  @Override
  public Set<Integer> getJumpTargets() {
    Set<Integer> targets = new HashSet<>();
    for (Case aCase : cases) {
      targets.add(aCase.getBasicBlockIndex());
    }
    if(defaultCase != null) {
      targets.add(defaultCase.getBasicBlockIndex());
    }
    return targets;
  }
  
  public GimpleExpr getValue() {
    return value;
  }

  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitSwitch(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("gimple_switch<").append(value).append("\n");

    Joiner.on("\n").appendTo(sb, cases);
    if(defaultCase!=null) {
      sb.append(String.format("\nDefault: goto %d",defaultCase.basicBlockIndex));
    }
    return sb.toString();
  }
}
