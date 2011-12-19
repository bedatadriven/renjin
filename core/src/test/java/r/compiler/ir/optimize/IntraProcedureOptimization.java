package r.compiler.ir.optimize;

import r.compiler.cfg.ControlFlowGraph;

public interface IntraProcedureOptimization {

  void optimize(ControlFlowGraph cfg);
  
}
