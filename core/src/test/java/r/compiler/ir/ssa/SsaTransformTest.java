package r.compiler.ir.ssa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.Iterables;

import r.compiler.CompilerTestCase;
import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.CfgPredicates;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.cfg.DominanceTree;
import r.compiler.ir.tac.IRBlock;
import r.compiler.ir.tac.operand.Variable;

public class SsaTransformTest extends CompilerTestCase {

  @Test
  public void cytronSsa() throws IOException {
    IRBlock block = parseCytron();
    ControlFlowGraph cfg = new ControlFlowGraph(block);

    Iterable<BasicBlock> assignmentsToK = Iterables.filter(cfg.getBasicBlocks(), 
        CfgPredicates.containsAssignmentTo(new Variable("K")));
    
    assertThat(Iterables.size(assignmentsToK), equalTo(3));
    
    
    DominanceTree dtree = new DominanceTree(cfg);
    
    SsaTransformer.insertPhiFunctions(cfg, dtree);
    
    // See Figure 6 in
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf
    
    
    // just before branching in basic block #2,
    // we need phi functions for all 4 variables
    
    BasicBlock bb2 = cfg.getBasicBlocks().get(1);
    assertThat(bb2.getStatements().size(), equalTo(5));
   
    System.out.println(cfg);
  }
  
}
