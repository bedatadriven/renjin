package org.renjin.gcc.gimple;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.gimple.statement.GimpleReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GimpleBasicBlock {

  private int index;
  private List<GimpleStatement> instructions = Lists.newArrayList();

  public GimpleBasicBlock() {
  }
  
  public GimpleBasicBlock(GimpleStatement... statements) {
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
    for (GimpleStatement ins : instructions) {
      sb.append("  ").append(ins).append("\n");
    }
    return sb.toString();
  }

  public void addIns(GimpleStatement ins) {
    instructions.add(ins);
  }
  

  public List<GimpleStatement> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<GimpleStatement> instructions) {
    this.instructions = instructions;
  }

  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    for (GimpleStatement instruction : instructions) {
      instruction.replaceAll(predicate, newExpr);
    }
  }

  public <T extends GimpleStatement> Iterable<T> getInstructions(Class<T> insClass) {
    return Iterables.filter(instructions, insClass);
  }

  public GimpleStatement getLast() {
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
