package org.renjin.compiler.ir.optimize;

import org.renjin.compiler.cfg.ControlFlowGraph;

public interface IntraProcedureOptimization {

  void optimize(ControlFlowGraph cfg);
  
}
