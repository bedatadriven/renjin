package org.renjin.compiler.cfg;

import org.junit.Test;
import org.renjin.compiler.CompilerTestCase;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.statements.Statement;

import static org.junit.Assert.assertFalse;

public class LiveSetTest extends CompilerTestCase {

  
  @Test
  public void test() {

    IRBody body = buildBody("x <- numeric(100); for(i in 1:100) x[i] <- i");
    ControlFlowGraph cfg = new ControlFlowGraph(body);
    DominanceTree tree = new DominanceTree(cfg);
    
    SsaTransformer transformer = new SsaTransformer(cfg, tree);
    transformer.transform();

    System.out.println(cfg);
    
    UseDefMap useDefMap = new UseDefMap(cfg);
    
    LiveSet liveSet = new LiveSet(tree, useDefMap);
    
    BasicBlock loopBody = cfg.get("BB3");
    Statement replaceStatement = loopBody.getStatements().get(1);
  
    // Replace statement should now be
    // x₃ ← ([<- i₂ i₂)
    System.out.println(replaceStatement);
   
    // Verify that x₂ is NOT live-out
    SsaVariable x2 = new SsaVariable(new EnvironmentVariable("x"), 2);
    assertFalse(liveSet.isLiveOut(loopBody, replaceStatement, x2));
  
  }
  
}