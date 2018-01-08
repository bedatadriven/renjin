/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.statement.*;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sequence of {@code GimpleStatement}s with no branches in except to the entry and no branches out except at the exit.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Basic_block">Basic Block</a> on Wikipedia
 */
public class GimpleBasicBlock {

  private int index;
  
  private List<GimpleStatement> statements = Lists.newArrayList();
  private List<GimpleEdge> edges = Lists.newArrayList();

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

  public List<GimpleEdge> getEdges() {
    return edges;
  }

  public void setEdges(List<GimpleEdge> edges) {
    this.edges = edges;
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

  public List<GimpleEdge> getJumps() {
    if(edges.isEmpty()) {
      return Collections.emptyList();
    } else {
      return edges;
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
    if (lastStatement instanceof GimpleReturn ||
        lastStatement instanceof GimpleConditional ||
        lastStatement instanceof GimpleGoto) {
      return false;
    } else {
      
      // falling throughhhhhhhh.....
      return true;
    }
  }
  
  public void accept(GimpleExprVisitor visitor) {
    for (GimpleStatement statement : statements) {
      statement.accept(visitor);
    }
  }
}
