package org.renjin.gcc.gimple;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.ins.GimpleIns;
import org.renjin.gcc.gimple.ins.GimpleReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GimpleBasicBlock {

  private int index;
  private List<GimpleIns> instructions = Lists.newArrayList();

  public GimpleBasicBlock() {
  }
  
  public GimpleBasicBlock(GimpleIns... statements) {
    this.instructions.addAll(Arrays.asList(statements));
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public String getName() {
    return "" + index;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(index).append(">:\n");
    for (GimpleIns ins : instructions) {
      sb.append("  ").append(ins).append("\n");
    }
    return sb.toString();
  }

  public void addIns(GimpleIns ins) {
    instructions.add(ins);
  }
  

  public List<GimpleIns> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<GimpleIns> instructions) {
    this.instructions = instructions;
  }

  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    for (GimpleIns instruction : instructions) {
      instruction.replaceAll(predicate, newExpr);
    }
  }

  public <T extends GimpleIns> Iterable<T> getInstructions(Class<T> insClass) {
    return Iterables.filter(instructions, insClass);
  }

  public GimpleIns getLast() {
    return instructions.get(instructions.size() - 1);
  }

  /**
   * 
   * @return true if this basic block ends with a return statement.
   */
  public boolean isReturning() {
    if(instructions.isEmpty()) {
      return false;
    } else {
      return getLast() instanceof GimpleReturn;
    }
  }

  /**
   * 
   * @return the set of basic block indexes to which this statement might jump
   */
  public Set<Integer> getJumpTargets() {
    if(instructions.isEmpty()) {
      return Collections.emptySet();
    } else {
      return getLast().getJumpTargets();
    }
  }

  public boolean isEmpty() {
    return instructions.isEmpty();
  }
}
