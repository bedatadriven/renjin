package org.renjin.compiler.ir.ssa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.renjin.compiler.CompilerTestCase;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.CfgPredicates;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;


import com.google.common.collect.Iterables;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import polyglot.ast.Return;

public class SsaTransformTest extends CompilerTestCase {

  @Test
  public void cytronSsa() throws IOException {
    IRBody block = parseCytron();
    ControlFlowGraph cfg = new ControlFlowGraph(block);

    System.out.println(cfg);

    Iterable<BasicBlock> assignmentsToK = Iterables.filter(cfg.getBasicBlocks(),
        CfgPredicates.containsAssignmentTo(new EnvironmentVariable("K")));

    assertThat(Iterables.size(assignmentsToK), equalTo(3));


    DominanceTree dtree = new DominanceTree(cfg);
    System.out.println(dtree);

    SsaTransformer transformer = new SsaTransformer(cfg, dtree);
    transformer.transform();


    cfg.dumpGraph();

    // See Figure 6 in
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf


    // just before branching in basic block #2,
    // we need phi functions for all 4 variables

    BasicBlock bb2 = cfg.getBasicBlocks().get(2);
    assertThat(bb2.getStatements().size(), equalTo(5));

    System.out.println(cfg);
  }
  

  @Test
  public void forLoop() throws IOException {
    IRBody block = buildScope("for(i in 1:10) { n<-x[i]; print(n); }");
    
    System.out.println(block);
    
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
    DominanceTree dtree = new DominanceTree(cfg);
    
    System.out.println("CFG:");
    System.out.println(cfg.getGraph());
    
    System.out.println("Dominance Tree:");  
    System.out.println(dtree);
    
    SsaTransformer transformer = new SsaTransformer(cfg, dtree);
    transformer.transform();
     
    System.out.println(cfg);
  }

  @Test
  public void returnValue() {

    IRBody block = buildScope("x <- 1; for(i in 1:2) { x<-x+1 }; x;");
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    DominanceTree dtree = new DominanceTree(cfg);
    SsaTransformer transformer = new SsaTransformer(cfg, dtree);
    transformer.transform();

    System.out.println(cfg);

    ReturnStatement stmt = (ReturnStatement) cfg.getBasicBlocks().get(5).getStatements().get(0);
    assertThat(stmt.getRHS(), instanceOf(SsaVariable.class));
  }
}
