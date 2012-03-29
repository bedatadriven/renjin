package org.renjin.compiler.ir.ssa;

import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.CfgPredicates;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;


import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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


  public SsaTransformer(ControlFlowGraph cfg, DominanceTree dtree) {
    super();
    this.cfg = cfg;
    this.dtree = dtree;
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

    for(Variable V : cfg.variables()) {
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
              Y.insertPhiFunction(V, cfg.getPredecessors(Y).size());
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

    for(Variable V : cfg.variables()) {

      // in a deviation from Cytron, et. al,
      // V_0 represents the uninitialized value
      
      C.put(V, 1);

      Stack<Integer> stack = new Stack<Integer>();
      stack.push(0);
      S.put(V, stack);
    }

    search(cfg.getEntry());
  }

  private void search(BasicBlock X) {
    
    for(Statement stmt : X.getStatements()) {
      Expression rhs = stmt.getRHS();
      if(!(rhs instanceof PhiFunction)) {
        for(Variable V : rhs.variables()) {
          int i = Top(V);
          rhs = rhs.replaceVariable(V, new SsaVariable(V, i));
        }
        stmt = X.replaceStatement(stmt, stmt.withRHS(rhs));
      }
      
      if(stmt instanceof Assignment) {
        Assignment assignment = (Assignment)stmt;
        if(assignment.getLHS() instanceof Variable) {
          Variable V = (Variable)assignment.getLHS();
          int i = C.get(V);
          X.replaceStatement(assignment, assignment.withLHS(new SsaVariable(V, i)));
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
        rhs = rhs.replaceVariable(j, i);
        Y.replaceStatement(A, new Assignment(A.getLHS(), rhs));
        // replace the j-th operand V in RHS(F) by V_i where i = Top(S(V))
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
    for(BasicBlock P : cfg.getPredecessors(X)) {
      if(P.equals(Y)) {
        return j;
      }
      j++;
    }
    throw new IllegalArgumentException("X is not a predecessor of Y");
  }
}
