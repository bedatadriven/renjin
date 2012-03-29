package org.renjin.compiler.ir.optimize;

import java.util.List;

import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRFunction;
import org.renjin.compiler.ir.tac.IRProgram;

import com.google.common.collect.Lists;


/**
 * Optimizes an R program statically. (Ahead of time)
 * 
 */
public class StaticOptimizer {

  
  private final IRProgram program;
  private final List<IntraProcedureOptimization> ipOptimizations = Lists.newArrayList();
  
  public StaticOptimizer(IRProgram program) {
    this.program = program;
  }

  public void addOptimization(IntraProcedureOptimization optimization) {
    ipOptimizations.add(optimization);
  }
  
  public void optimize() {
    doIntraScopeOptimizations();
  }

  private void doIntraScopeOptimizations() {
    System.out.println("MAIN:");
    doIntraScopeOptimization(program.getMain());
    for(IRFunction function : program.getFunctions()) {
      System.out.println(function);
      doIntraScopeOptimization(function.getBody());
    }
  }

  private void doIntraScopeOptimization(IRBody main) {
    ControlFlowGraph cfg = new ControlFlowGraph(main);
    DominanceTree dtree = new DominanceTree(cfg);
    SsaTransformer ssaTransformer = new SsaTransformer(cfg, dtree);
    ssaTransformer.transform();
    
    System.out.println(cfg);
    
  }
}
