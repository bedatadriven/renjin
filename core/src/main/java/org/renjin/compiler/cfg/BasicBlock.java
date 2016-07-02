package org.renjin.compiler.cfg;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicBlock {
  private final IRBody parent;
  private String debugId;
  
  private Set<IRLabel> labels;
  private List<Statement> statements = Lists.newArrayList();
  
  List<BasicBlock> flowSuccessors = new ArrayList<>();
  List<BasicBlock> flowPredecessors = new ArrayList<>();

  List<BasicBlock> dominanceSuccessors = new ArrayList<>();
  List<BasicBlock> dominancePredecessors = new ArrayList<>();
  
  final Set<FlowEdge> outgoing = new HashSet<>();
  final Set<FlowEdge> incoming = new HashSet<>();
  
  public BasicBlock(IRBody parent) {
    super();
    this.parent = parent;
  }
  
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
  public void insertPhiFunction(Variable variable, Set<FlowEdge> incomingEdges) {
    statements.add(0, new Assignment(variable, new PhiFunction(variable, incomingEdges)));
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

  public Set<FlowEdge> getIncoming() {
    return incoming;
  }

  public Set<FlowEdge> getOutgoing() {
    return outgoing;
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
    return (Iterable)Iterables.filter(statements, Predicates.instanceOf(Assignment.class));
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
}
