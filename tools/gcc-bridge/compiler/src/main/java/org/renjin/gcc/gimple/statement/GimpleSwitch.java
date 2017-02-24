/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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

    public String toString() {
      StringBuilder s = new StringBuilder();
      s.append("case ");
      if(this.low == this.high) {
        s.append(this.low);
      } else {
        s.append(this.low).append("-").append(high);
      }
      s.append(": goto ");
      s.append(this.basicBlockIndex);
      return s.toString();
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
    sb.append("switch(").append(value).append(") {");
    Joiner.on("\n").appendTo(sb, cases);
    if(defaultCase!=null) {
      sb.append(String.format("\ndefault: goto %d",defaultCase.basicBlockIndex));
    }
    return sb.toString();
  }
}
