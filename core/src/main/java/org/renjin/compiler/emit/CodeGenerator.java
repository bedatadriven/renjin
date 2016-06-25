package org.renjin.compiler.emit;

import org.renjin.compiler.cfg.ControlFlowGraph;

/**
 * Generates the code for a loop body 
 */
public class CodeGenerator {
  
  private ControlFlowGraph cfg;

  public CodeGenerator(ControlFlowGraph cfg) {
    this.cfg = cfg;
  }
  
  
  
}
