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
package org.renjin.compiler.ir.ssa;

import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.*;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.Symbol;

import java.util.*;

/**
 * Transforms three-address IR code into 
 * single static assignment form.
 *
 */
public class SsaTransformer {

  private ControlFlowGraph cfg;
  private DominanceTree dtree;
  private final DominanceTree rdt;

  private Map<Variable, Integer> C = Maps.newHashMap();
  private Map<Variable, Stack<Integer>> S = Maps.newHashMap();
  private Set<Variable> allVariables;

  public SsaTransformer(ControlFlowGraph cfg) {
    super();
    this.cfg = cfg;
    this.dtree = new DominanceTree(cfg);
    this.rdt = new DominanceTree(new ReverseControlFlowGraph(cfg));
    this.allVariables = allVariables();
  }

  public void transform() {
    insertPhiFunctions();
    renameVariables();
  }

  public void insertEnvironmentUpdates() {
    Collection<BasicBlock> returningBlocks = rdt.getChildren(cfg.getExit());
    for (BasicBlock returningBlock : returningBlocks) {
      if (returningBlock != cfg.getEntry()) {
        insertEnvironmentUpdates(returningBlock, returningBlock, Collections.emptySet());
      }
    }
  }

  private void insertEnvironmentUpdates(BasicBlock target, BasicBlock block, Set<Symbol> alreadyInserted) {

    Set<Symbol> inserted = Sets.newHashSet(alreadyInserted);

    List<Statement> statements = block.getStatements();
    List<Statement> updates = new ArrayList<>();

    for (int i = statements.size() - 1; i >= 0; i--) {
      Statement statement = statements.get(i);
      if (statement instanceof Assignment) {
        Assignment assignment = (Assignment) statement;
        if (assignment.getLHS() instanceof EnvironmentVariable) {
          EnvironmentVariable var = (EnvironmentVariable) assignment.getLHS();
          if(!(assignment.getRHS() instanceof ReadEnvironment)) {
            Symbol name = var.getName();
            if (inserted.add(name)) {
              updates.add(new UpdateEnvironment(name, var));
            }
          }
        }
      }
    }

    if(target.fallsThrough()) {
      target.getStatements().addAll(updates);
    } else {
      target.getStatements().addAll(target.getStatements().size() - 1, updates);
    }


    Collection<BasicBlock> successors = rdt.getChildren(block);
    for (BasicBlock successor : successors) {
      if(successors.size() > 1) {
        insertEnvironmentUpdates(successor, successor, inserted);
      } else {
        insertEnvironmentUpdates(target, successor, inserted);
      }
    }
  }

  /**
   * Inserts PHI functions at the beginning of basic blocks. 
   */
  private void insertPhiFunctions() {

    // See Figure 11
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf


    // HasAlready(*) is an array of flags, one for each node, where HasAlready(X)
    // indicates whether a Φ-function for V has already been inserted at X

    // Work(*) is an array of flags, one flag for each node, where Work(X)
    // indicates whether X has ever been added to W during the current iteration
    // of the outer loop.

    // These two flags could have been implemented with just
    // the values true and false, but this would require additional record keeping
    // to reset any true flags between iterations, without the expense of looping
    // over all the nodes. It is simpler to devote an integer to each flag and to test
    // flags by comparing them with the current iteration count.

    int hasAlready[] = new int[cfg.getBasicBlocks().size()];
    int work[] = new int[cfg.getBasicBlocks().size()];


    Queue<BasicBlock> W = Lists.newLinkedList();

    int iterCount = 0;
    for(Variable V : allVariables) {
      iterCount = iterCount + 1;

      for(BasicBlock X : Iterables.filter(cfg.getBasicBlocks(), CfgPredicates.containsAssignmentTo(V))) {
        work[X.getIndex()] = iterCount;
        W.add(X);
      }
      while(!W.isEmpty()) {
        BasicBlock X = W.poll();
        for(BasicBlock Y : dtree.getFrontier(X)) {
          if(hasAlready[Y.getIndex()] < iterCount) {

            // place (V <- phi(V,..., V)) at Y
            Y.insertPhiFunction(V, Y.getIncoming());

            hasAlready[Y.getIndex()] = iterCount;
            if(work[Y.getIndex()] < iterCount) {
              work[Y.getIndex()] = iterCount;
              W.add(Y);
            }
          }
        }
      }
    }
  }

  private void renameVariables() {
    // Figure 12 in
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf

    for(Variable V : allVariables) {

      // in a deviation from Cytron, et. al,
      // V_0 represents the uninitialized value

      C.put(V, 1);

      Stack<Integer> stack = new Stack<Integer>();
      stack.push(0);
      S.put(V, stack);
    }

    search(cfg.getEntry());
  }

  private Set<Variable> allVariables() {
    Set<Variable> set = Sets.newHashSet();
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      for(Statement statement : bb.getStatements()) {
        collectVariables(set, statement.getRHS());
        if(statement instanceof  Assignment) {
          collectVariables(set, ((Assignment) statement).getLHS());
        }
      }
    }
    return set;
  }

  private void search(BasicBlock X) {

    for(Statement stmt : X.getStatements()) {
      renameVariables(stmt);


      if(stmt instanceof Assignment) {
        Assignment assignment = (Assignment)stmt;
        if(assignment.getLHS() instanceof Variable) {
          Variable V = (Variable)assignment.getLHS();
          int i = C.get(V);
          assignment.setLHS(V.getVersion(i));
          S.get(V).push(i);
          C.put(V, i + 1);
        }
      }
    }

    for(BasicBlock Y : cfg.getSuccessors(X)) {
      int j = whichPred(Y,X);
      for (Assignment A : Lists.newArrayList(Y.phiAssignments())) {
        PhiFunction rhs = (PhiFunction) A.getRHS();
        Variable V = rhs.getArgument(j);
        int i = Top(V);
        // replace the j-th operand V in RHS(F) by V_i where i = Top(S(V))
        rhs.setVersionNumber(j, i);
      }
    }
    for(BasicBlock Y : dtree.getChildren(X)) {
      search(Y);
    }
    for(Assignment A : X.assignments()) {
      if(A.getLHS() instanceof SsaVariable) {
        SsaVariable lhs = (SsaVariable) A.getLHS();
        S.get(lhs.getInner()).pop();
      }
    }
  }

  private void renameVariables(TreeNode node) {
    if(node instanceof PhiFunction) {
      return;
    }

    for(int childIndex = 0; childIndex!=node.getChildCount();++childIndex) {
      Expression child = node.childAt(childIndex);
      if(child instanceof Variable) {
        Variable V = (Variable)child;
        int i = Top(V);
        node.setChild(childIndex, V.getVersion(i));
      } else if(child.getChildCount() > 0) {
        renameVariables(child);
      }
    }
  }

  private void collectVariables(Set<Variable> set, Expression rhs) {
    if(rhs instanceof Variable) {
      set.add((Variable)rhs);
    } else {
      for(int i=0;i!=rhs.getChildCount();++i) {
        collectVariables(set, rhs.childAt(i));
      }
    }
  }

  private int Top(Variable V) {
    Stack<Integer> stack = S.get(V);
    if(stack == null || stack.isEmpty()) {
      throw new IllegalStateException("Variable " + V + " has not been assigned to before its use");
    }
    return stack.peek();
  }

  /**
   * @return an integer telling which predecessor of Y in CFG is
   * X. The jth operand of a phi-function in Y corresponds to the 
   * jth predecessor
   * of Y from the listing of the in edges of Y
   */
  private int whichPred(BasicBlock X, BasicBlock Y) {
    int j = 0;
    for(FlowEdge P : X.getIncoming()) {
      if(P.getPredecessor().equals(Y)) {
        return j;
      }
      j++;
    }
    throw new IllegalArgumentException("X is not a predecessor of Y");
  }


  public void removePhiFunctions(TypeSolver types) {
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      if (bb != cfg.getExit()) {
        // Remove and collect phi statements
        List<Assignment> phiAssignments = new ArrayList<>();
        ListIterator<Statement> it = bb.getStatements().listIterator();
        while (it.hasNext()) {
          Statement statement = it.next();
          if (statement instanceof Assignment && statement.getRHS() instanceof PhiFunction) {
            phiAssignments.add((Assignment) statement);
            it.remove();
          }
        }
        // Insert assignments
        for (Assignment assignment : phiAssignments) {
          if(types.isUsed(assignment)) {
            insertAssignments(assignment.getLHS(), (PhiFunction) assignment.getRHS());
          }
        }
      }
    }
  }

  private void insertAssignments(LValue lhs, PhiFunction phi) {
    for (int i = 0; i < phi.getArguments().size(); i++) {
      SsaVariable variable = (SsaVariable)phi.getArgument(i);

      FlowEdge incoming = phi.getIncomingEdges().get(i);
      BasicBlock definingBlock = incoming.getPredecessor();
      definingBlock.addStatementBeforeJump(new Assignment(lhs, variable));
    }
  }


}
