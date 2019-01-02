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
import org.renjin.repackaged.guava.base.Predicates;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class BasicBlock {
  private final IRBody parent;
  private String debugId;
  
  private Set<IRLabel> labels;
  private List<Statement> statements = Lists.newLinkedList();
  
  List<BasicBlock> flowSuccessors = new ArrayList<>();
  List<BasicBlock> flowPredecessors = new ArrayList<>();

  List<BasicBlock> dominanceSuccessors = new ArrayList<>();
  List<BasicBlock> dominancePredecessors = new ArrayList<>();
  
  final List<FlowEdge> outgoing = new ArrayList<>();
  final List<FlowEdge> incoming = new ArrayList<>();

  private boolean live = false;
  
  public BasicBlock(IRBody parent) {
    super();
    this.parent = parent;
  }
  
  public void addStatement(Statement statement) {
    statements.add(statement);
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

  public Statement replaceStatement(Statement stmt, Statement newStmt) {
    int i = statements.indexOf(stmt);
    statements.set(i, newStmt);
    return newStmt;
  }
 
  public void replaceStatement(int i, Statement stmt) {
    statements.set(i, stmt);
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
  
  public static BasicBlock createWithStartAt(IRBody parent, int statementIndex) {
    BasicBlock block = new BasicBlock(parent);
    block.labels = parent.getIntructionLabels(statementIndex);
    block.statements = Lists.newArrayList();
    block.statements.add(parent.getStatements().get(statementIndex));
    return block;
  }

  public Set<IRLabel> getLabels() {
    return labels;
  }
  
  public boolean isLabeled() {
    return !labels.isEmpty();
  }
  
  public Statement getTerminal() {
    return statements.get(statements.size() - 1);
  }

  public void addFlowSuccessor(BasicBlock successor) {
    FlowEdge edge = new FlowEdge(this, successor);
    outgoing.add(edge);
    flowSuccessors.add(successor);
    successor.incoming.add(edge);
    successor.flowPredecessors.add(this);
  }

  public void addDominanceSuccessor(BasicBlock basicBlock) {
    dominanceSuccessors.add(basicBlock);
    basicBlock.dominancePredecessors.add(this);
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

  public List<BasicBlock> getFlowSuccessors() {
    return flowSuccessors;
  }

  public List<BasicBlock> getFlowPredecessors() {
    return flowPredecessors;
  }

  public List<BasicBlock> getDominanceSuccessors() {
    return dominanceSuccessors;
  }
  
  public List<BasicBlock> getDominancePredecessors() {
    return dominancePredecessors;
  }

  public boolean returns() {
    return getTerminal() instanceof ReturnStatement;
  }
  
  public boolean fallsThrough() {
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
    flowPredecessors.retainAll(live);
    flowSuccessors.retainAll(live);

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
