package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;


public class TypeResolver {

  public void resolveType(ControlFlowGraph cfg, VariableMap variableMap) {
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      if(bb != cfg.getExit()) {
        for(Statement statement : bb.getStatements()) {
          statement.getRHS().resolveType(variableMap);
          if(statement instanceof Assignment) {
            ((Assignment) statement).getLHS().resolveType(variableMap);
          }
        }
      }
    }
  }
}
