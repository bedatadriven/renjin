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
package org.renjin.compiler.ir.ssa;

import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.cfg.*;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.*;

/**
 * Transforms three-address IR code into 
 * single static assignment form.
 *
 */
public class SsaTransformer {

  private ControlFlowGraph cfg;
  private DominanceTree dtree;

  private Map<Variable, Integer> C = Maps.newHashMap();
  private Map<Variable, Stack<Integer>> S = Maps.newHashMap();
  private Set<Variable> allVariables;

  public SsaTransformer(ControlFlowGraph cfg, DominanceTree dtree) {
    super();
    this.cfg = cfg;
    this.dtree = dtree;
    this.allVariables = allVariables();
  }

  public void transform() {
    insertPhiFunctions();
    renameVariables();
  }

  /**
   * Inserts PHI functions at the beginning of basic blocks. 
   */
  private void insertPhiFunctions() {

    // See Figure 11
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf

    int iterCount = 0;

    Map<BasicBlock, Integer> hasAlready = Maps.newHashMap();
    Map<BasicBlock, Integer> work = Maps.newHashMap();

    for(BasicBlock X : cfg.getLiveBasicBlocks()) {
      hasAlready.put(X, 0);
      work.put(X, 0);
    }

    Queue<BasicBlock> W = Lists.newLinkedList();
    
    for(Variable V : allVariables) {
      iterCount = iterCount + 1;

      for(BasicBlock X : Iterables.filter(cfg.getLiveBasicBlocks(), CfgPredicates.containsAssignmentTo(V))) {
        work.put(X, iterCount);
        W.add(X);
      }
      while(!W.isEmpty()) {
        BasicBlock X = W.poll();
        for(BasicBlock Y : dtree.getFrontier(X)) {
          if(X != cfg.getExit()) {
            if(hasAlready.get(Y) < iterCount) {
              Y.insertPhiFunction(V, Y.getIncoming());
              // place (V <- phi(V,..., V)) at Y
              hasAlready.put(Y, iterCount);
              if(work.get(Y) < iterCount) {
                work.put(Y, iterCount);
                W.add(Y);
              }
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
    if(stack.isEmpty()) {
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
        ListIterator<Statement> it = bb.getStatements().listIterator();
        while (it.hasNext()) {
          Statement statement = it.next();
          if (statement instanceof Assignment && statement.getRHS() instanceof PhiFunction) {
            Assignment assignment = (Assignment) statement;
            if(types.isUsed(assignment)) {
              insertAssignments(assignment.getLHS(), (PhiFunction) statement.getRHS());
            }
            it.remove();
          }
        }
      }
    }
  }

  private void insertAssignments(LValue lhs, PhiFunction phi) {
    for (int i = 0; i < phi.getArguments().size(); i++) {
      SsaVariable variable = (SsaVariable)phi.getArgument(i);

      if(variable.getVersion() == 0) {
        cfg.getEntry().addStatement(new Assignment(lhs, variable));
      } else {
        FlowEdge incoming = phi.getIncomingEdges().get(i);
        BasicBlock definingBlock = incoming.getPredecessor();
        definingBlock.addStatementBeforeJump(new Assignment(lhs, variable));
      }
    }
  }
}
