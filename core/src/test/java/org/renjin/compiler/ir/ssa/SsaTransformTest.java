/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.compiler.ir.ssa;

import org.junit.Test;
import org.renjin.compiler.CompilerTestCase;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.CfgPredicates;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.repackaged.guava.collect.Iterables;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class SsaTransformTest extends CompilerTestCase {

  @Test
  public void cytronSsa() throws IOException {
    IRBody block = parseCytron();
    ControlFlowGraph cfg = new ControlFlowGraph(block);

    cfg.dumpEdges();
    
    System.out.println(cfg);

    Iterable<BasicBlock> assignmentsToK = Iterables.filter(cfg.getBasicBlocks(),
        CfgPredicates.containsAssignmentTo(new EnvironmentVariable("K")));

    assertThat(Iterables.size(assignmentsToK), equalTo(3));

    System.out.println("PREDECESSORS:");
    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      System.out.println(basicBlock.getDebugId() + " => " + cfg.getPredecessors(basicBlock));
    }
    System.out.println("SUCESSORS:");
    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      System.out.println(basicBlock.getDebugId() + " => " + cfg.getSuccessors(basicBlock));
    }

    DominanceTree dtree = new DominanceTree(cfg);
    System.out.println(dtree);

    SsaTransformer transformer = new SsaTransformer(cfg);
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
    IRBody block = buildBody("for(i in 1:10) { n<-x[i]; print(n); }");
    
    System.out.println(block);
    
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
    DominanceTree dtree = new DominanceTree(cfg);
    
    System.out.println("CFG:");
    System.out.println(cfg);
    
    System.out.println("Dominance Tree:");  
    System.out.println(dtree);
    
    SsaTransformer transformer = new SsaTransformer(cfg);
    transformer.transform();
     
  }
  
  @Test
  public void forLoop2() {
    IRBody block = buildBody("s <- 9; for(i in 1:1000) { s <- s + sqrt(i) }");

    ControlFlowGraph cfg = new ControlFlowGraph(block);
    DominanceTree dtree = new DominanceTree(cfg);
    SsaTransformer transformer = new SsaTransformer(cfg);
    transformer.transform();
    
    System.out.println(cfg);
  }

  @Test
  public void returnValue() {

    IRBody block = buildBody("x <- 1; for(i in 1:2) { x<-x+1 }; x;");
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    DominanceTree dtree = new DominanceTree(cfg);
    SsaTransformer transformer = new SsaTransformer(cfg);
    transformer.transform();

    System.out.println(cfg);

    ReturnStatement stmt = (ReturnStatement) cfg.getBasicBlocks().get(5).getTerminal();
    assertThat(stmt.getRHS(), instanceOf(SsaVariable.class));
  }
}
