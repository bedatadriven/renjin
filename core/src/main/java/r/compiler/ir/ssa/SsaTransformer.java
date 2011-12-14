package r.compiler.ir.ssa;

import java.util.Map;
import java.util.Queue;

import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.CfgPredicates;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.cfg.DominanceTree;
import r.compiler.ir.tac.operand.Variable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
 
/**
 * Transforms three-address IR code into 
 * single static assignment form.
 *
 */
public class SsaTransformer {

  private SsaTransformer() {}
  
  /**
   * Inserts PHI functions at the beginning of basic blocks. 
   */
  public static void insertPhiFunctions(ControlFlowGraph cfg, DominanceTree dtree) {
    
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
          if(hasAlready.get(Y) < iterCount) {
            Y.insertPhiFunction(V);
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
