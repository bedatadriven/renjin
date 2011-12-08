package r.compiler.cfg;

import java.util.List;

import r.compiler.ir.tac.IRBlock;

public class ControlFlowGraph {

  private final IRBlock block;
  private final List<BasicBlock> basicBlocks;
  
  public ControlFlowGraph(IRBlock block) {
    this.block = block;
    this.basicBlocks = BasicBlockAlgorithm.basicBlocks(block);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=basicBlocks.size();++i) {
      sb.append("\nBASIC BLOCK " + i + " ====================\n");
      sb.append(basicBlocks.get(i));
    }
    return sb.toString();
  }

}
