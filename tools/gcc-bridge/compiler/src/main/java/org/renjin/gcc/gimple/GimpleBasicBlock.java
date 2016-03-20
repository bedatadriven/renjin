package org.renjin.gcc.gimple;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.GimpleConditional;
import org.renjin.gcc.gimple.statement.GimpleGoto;
import org.renjin.gcc.gimple.statement.GimpleReturn;
import org.renjin.gcc.gimple.statement.GimpleStatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Sequence of {@code GimpleStatement}s with no branches in except to the entry and no branches out except at the exit.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Basic_block">Basic Block</a> on Wikipedia
 */
public class GimpleBasicBlock {

  private int index;
  
  private List<GimpleStatement> statements = Lists.newArrayList();

  public GimpleBasicBlock() {
  }
  
  public GimpleBasicBlock(GimpleStatement... statements) {
    this.statements.addAll(Arrays.asList(statements));
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
    for (GimpleStatement ins : statements) {
      sb.append("  ").append(ins).append("\n");
    }
    return sb.toString();
  }

  /**
   * 
   * @return the statements in this basic block
   */
  public List<GimpleStatement> getStatements() {
    return statements;
  }

  public void setStatements(List<GimpleStatement> statements) {
    this.statements = statements;
  }

  /**
   * Replaces all {@link GimpleExpr}s within this basic block that match the given {@code predicate} with
   * the given {@code newExpr}.
   */
  public void replaceAll(Predicate<? super GimpleExpr> predicate, GimpleExpr newExpr) {
    for (GimpleStatement instruction : statements) {
      instruction.replaceAll(predicate, newExpr);
    }
  }

  /**
   * 
   * @return the last {@code GimpleStatement} in this basic block
   */
  public GimpleStatement getLast() {
    return statements.get(statements.size() - 1);
  }

  /**
   * 
   * @return true if this basic block ends with a return statement.
   */
  public boolean isReturning() {
    if(statements.isEmpty()) {
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
    if(statements.isEmpty()) {
      return Collections.emptySet();
    } else {
      return getLast().getJumpTargets();
    }
  }

  /**
   * 
   * @return {@code true} if this basic block is empty
   */
  public boolean isEmpty() {
    return statements.isEmpty();
  }

  public boolean fallsThrough() {
    if(statements.isEmpty()) {
      return true;
    }
    GimpleStatement lastStatement = statements.get(statements.size() - 1);
    if(lastStatement instanceof GimpleReturn ||
       lastStatement instanceof GimpleConditional ||
       lastStatement instanceof GimpleGoto) {
      return false;
    } else {
      
      // falling throughhhhhhhh.....
      return true;
    }
  }
}
