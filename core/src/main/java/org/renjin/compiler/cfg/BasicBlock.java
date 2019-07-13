/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.cfg;

import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.*;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.*;

/**
 * Represents a straight-line piece of code without any jumps or jump targets.
 * Jump targets start a block, and jumps end a block.
 */
public class BasicBlock {

  int index;
  private String debugId;
  
  private Set<IRLabel> labels;
  private List<Statement> statements = Lists.newLinkedList();
  
  private List<BasicBlock> successors = new ArrayList<>();
  private List<BasicBlock> predecessors = new ArrayList<>();

  private final List<FlowEdge> outgoing = new ArrayList<>();
  private final List<FlowEdge> incoming = new ArrayList<>();

  private boolean live = false;
  
  BasicBlock(int index) {
    this.index = index;
    this.labels = Collections.emptySet();
  }

  public int getIndex() {
    return index;
  }

  public void addStatement(Statement statement) {
    statements.add(statement);
    statement.setBasicBlock(this);
  }
  
  public void insertPhiFunction(Variable variable, List<FlowEdge> incomingEdges) {
    statements.add(0, new Assignment(variable, new PhiFunction(variable, incomingEdges)));
  }

  public boolean isLive() {
    return live;
  }

  public void setLive(boolean live) {
    this.live = live;
  }

  public List<Statement> getStatements() {
    return statements;
  }
  
  public void setDebugId(int index) {
    this.debugId = "BB" + index;
  }

  public void setDebugId(String string) {
    this.debugId = string;
  }
  
  public static BasicBlock createWithStartAt(int blockIndex, IRBody parent, int statementIndex) {
    BasicBlock block = new BasicBlock(blockIndex);
    block.labels = parent.getInstructionLabels(statementIndex);
    block.statements = Lists.newArrayList();
    block.statements.add(parent.getStatements().get(statementIndex));
    return block;
  }

  public Set<IRLabel> getLabels() {
    return labels;
  }

  public Statement getTerminal() {
    return statements.get(statements.size() - 1);
  }

  public void addFlowSuccessor(BasicBlock successor) {
    FlowEdge edge = new FlowEdge(this, successor);
    outgoing.add(edge);
    successors.add(successor);
    successor.incoming.add(edge);
    successor.predecessors.add(this);
  }

  public List<FlowEdge> getIncoming() {
    return incoming;
  }

  public List<FlowEdge> getOutgoing() {
    return outgoing;
  }

  public FlowEdge getOutgoing(IRLabel target) {
    for (FlowEdge flowEdge : outgoing) {
      if(flowEdge.getSuccessor().getLabels().contains(target)) {
        return flowEdge;
      }
    }
    throw new IllegalStateException("No outgoing edge to " + target);
  }

  public List<BasicBlock> getSuccessors() {
    return successors;
  }

  public List<BasicBlock> getPredecessors() {
    return predecessors;
  }

  public boolean returns() {
    return getTerminal() instanceof ReturnStatement;
  }
  
  public boolean fallsThrough() {
    if(statements.isEmpty()) {
      return true;
    }
    Statement terminal = getTerminal();
    return !( terminal instanceof GotoStatement ||
              terminal instanceof IfStatement ||
              terminal instanceof ReturnStatement);
  }
  
  public Iterable<IRLabel> targets() {
    return getTerminal().possibleTargets();
  }

  public String statementsToString() {
    StringBuilder sb = new StringBuilder();
    for(Statement statement : statements) {
      sb.append(statement).append("\n");
    }
    return sb.toString();
  }

  public Iterable<Assignment> assignments() {
    return Iterables.filter(statements, Assignment.class);
  }
  
  public Iterable<Assignment> phiAssignments() {
    return (Iterable)Iterables.filter(statements, CfgPredicates.isPhiAssignment());
  }
  
  @Override
  public String toString() {
    return debugId;
  }

  public String getDebugId() {
    return debugId;
  }

  public void addStatementBeforeJump(Assignment assignment) {
    int pos = getStatements().size();
    if(!fallsThrough()) {
      pos = pos - 1;
    }
    getStatements().add(pos, assignment);
  }

  @Override
  public int hashCode() {
    // Use a stable hash code to get consistent results
    return debugId.hashCode();
  }

  public void removeDeadEdges(Set<BasicBlock> live) {
    predecessors.retainAll(live);
    successors.retainAll(live);

    ListIterator<FlowEdge> incomingIt = incoming.listIterator();
    while(incomingIt.hasNext()) {
      if(!live.contains(incomingIt.next().getPredecessor())) {
        incomingIt.remove();
      }
    }
    
    ListIterator<FlowEdge> outgoingIt = outgoing.listIterator();
    while(outgoingIt.hasNext()) {
      if(!live.contains(outgoingIt.next().getSuccessor())) {
        outgoingIt.remove();
      }
    }

  }

  public boolean isPure() {
    for (Statement statement : statements) {
      if(!statement.isPure()) {
        return false;
      }
    }
    return true;
  }
}
