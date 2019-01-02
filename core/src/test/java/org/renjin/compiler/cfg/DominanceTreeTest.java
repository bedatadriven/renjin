/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.cfg;

import org.junit.Test;
import org.renjin.compiler.CompilerTestCase;
import org.renjin.compiler.ir.tac.IRBody;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class DominanceTreeTest extends CompilerTestCase {


  @Test
  public void immediateDominators() {
    IRBody block = buildBody("y<-1; if(q) y<-y+1 else y<-4; y");
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
    System.out.println(cfg);
    
    BasicBlock bb0 = cfg.getBasicBlocks().get(1); // y <- 1; if q goto BB1 else BB2
    BasicBlock bb1 = cfg.getBasicBlocks().get(2); // y <- y + 1
    BasicBlock bb2 = cfg.getBasicBlocks().get(3); // y <- 4
    BasicBlock bb3 = cfg.getBasicBlocks().get(4); // return y;
    
    DominanceTree domTree = new DominanceTree(cfg);
    assertThat(domTree.getImmediateDominator(bb1), equalTo(bb0));
    assertThat(domTree.getImmediateDominator(bb2), equalTo(bb0));
    assertThat(domTree.getImmediateDominator(bb3), equalTo(bb0));
    assertThat(domTree.getImmediateDominator(cfg.getExit()), equalTo(cfg.getEntry()));
  }
  
  @Test
  public void dominanceFrontier() throws IOException {
    IRBody block = parseCytron();
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    cfg.dumpGraph();


    List<BasicBlock> bb = cfg.getLiveBasicBlocks();
    DominanceTree dtree = new DominanceTree(cfg);
    dtree.dumpGraph();

    // See Figure 9 in
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf
    
    assertThat(dtree.getFrontier(bb.get(2)), itemsEqualTo(bb.get(2), cfg.getExit()));
    assertThat(dtree.getFrontier(bb.get(3)), itemsEqualTo(bb.get(8)));
    assertThat(dtree.getFrontier(bb.get(4)), itemsEqualTo(bb.get(6)));
    assertThat(dtree.getFrontier(bb.get(5)), itemsEqualTo(bb.get(6)));
    assertThat(dtree.getFrontier(bb.get(6)), itemsEqualTo(bb.get(8)));
    assertThat(dtree.getFrontier(bb.get(7)), itemsEqualTo(bb.get(8)));
    assertThat(dtree.getFrontier(bb.get(8)), itemsEqualTo(bb.get(2), cfg.getExit()));
    assertThat(dtree.getFrontier(bb.get(9)), itemsEqualTo(bb.get(2), bb.get(9), cfg.getExit()));
    assertThat(dtree.getFrontier(bb.get(10)), itemsEqualTo(bb.get(11)));
    assertThat(dtree.getFrontier(bb.get(11)), itemsEqualTo(bb.get(2), bb.get(9), cfg.getExit()));
    assertThat(dtree.getFrontier(bb.get(12)), itemsEqualTo(bb.get(2), cfg.getExit()));
  } 
}
