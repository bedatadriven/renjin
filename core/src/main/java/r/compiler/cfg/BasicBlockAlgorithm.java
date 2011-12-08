package r.compiler.cfg;

import java.util.List;

import r.compiler.ir.tac.IRBlock;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.instructions.GotoStatement;
import r.compiler.ir.tac.instructions.IfStatement;
import r.compiler.ir.tac.instructions.Statement;

import com.google.common.collect.Lists;

/**
 * Determines the basic blocks within an IRBlock.
 * See http://en.wikipedia.org/wiki/Basic_block#Algorithm_to_generate_basic_blocks.
 */
public class BasicBlockAlgorithm {

  static boolean[] markLeaders(IRBlock block) {

    List<Statement> statements = block.getStatements();

    // label basic blocks
    boolean leader[] = new boolean[statements.size()];

    // (1) first instruction is a leader
    leader[0] = true;

    // (2) The target of a conditional or an unconditional
    // goto/jump instruction is a leader.

    // (3) The instruction that immediately follows a conditional or an 
    // unconditional goto/jump instruction is a leader.

    for(int i=0;i!=leader.length;++i) {
      Statement stmt = statements.get(i);
      if(stmt instanceof IfStatement) {
        IfStatement ifStmt = (IfStatement) stmt;
        leader[block.getLabelInstructionIndex(ifStmt.getTrueTarget())] = true;
        leader[block.getLabelInstructionIndex(ifStmt.getFalseTarget())] = true;
        
        if(i+1 < leader.length) {
          leader[i+1] = true;
        }
        
      } else if(stmt instanceof GotoStatement) {        
        IRLabel target = ((GotoStatement) stmt).getTarget();
        leader[block.getLabelInstructionIndex(target)] = true;
        
        if(i+1 < leader.length) {
          leader[i+1] = true;
        }
      }
    }
    return leader;
  }
  
  public static List<BasicBlock> basicBlocks(IRBlock block) {
    boolean isLeader[] = markLeaders(block);
    List<BasicBlock> basicBlocks = Lists.newArrayList();
    int start = 0;
    while(start < isLeader.length) {
      int blockEnd = findBasicBlockEnd(isLeader, start);
      basicBlocks.add(new BasicBlock(block, start, blockEnd));
      start = blockEnd;
    }
    return basicBlocks;
  }
  
  private static int findBasicBlockEnd(boolean leader[], int leaderIndex) {
    for(int i=leaderIndex+1;i<leader.length;++i) {
      if(leader[i]) {
        return i;
      }
    }
    // includes end of block
    return leader.length;
  }
}
