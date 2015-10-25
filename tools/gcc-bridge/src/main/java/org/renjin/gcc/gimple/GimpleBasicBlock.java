package org.renjin.gcc.gimple;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.ins.GimpleIns;

import java.util.List;

public class GimpleBasicBlock {

  private int index;
  private List<GimpleIns> instructions = Lists.newArrayList();

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

  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    for (GimpleIns instruction : instructions) {
      instruction.replaceAll(predicate, newExpr);
    }
  }

  public <T extends GimpleIns> Iterable<T> getInstructions(Class<T> insClass) {
    return Iterables.filter(instructions, insClass);
  }
}
