/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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


public class ControlFlowGraphTest extends CompilerTestCase {

  @Test
  public void singleBlock() {
    IRBody block = buildBody("y<-x+1;z<-3; 4");
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
    System.out.println(cfg);
    
    List<BasicBlock> basicBlocks = cfg.getBasicBlocks();
    assertThat(basicBlocks.size(), equalTo(3));  // entry + 1 + exit = 3
    assertThat(basicBlocks.get(1).getStatements().size(), equalTo(block.getStatements().size()));
  }
  
  @Test
  public void forLoop() {
    IRBody block = buildBody("y <- 0; for(i in 1:10) y <- y + i; sqrt(y + 3 * x)");
    System.out.println(block);

    
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    System.out.println(cfg);

    List<BasicBlock> basicBlocks = cfg.getBasicBlocks();
    assertThat(basicBlocks.size(), equalTo(7));
  }
  
  @Test
  public void forBlock() {
    IRBody block = buildBody("if(length(x)==1) FALSE else { y<-0; for(i in seq_along(x)) y <- y+1 }");
    System.out.println(block);
//    
//
//    assertThat(leaders, equalTo(new boolean[]{    
//         true,    //      0:  _t0 := dynamic<length>(x)
//         false,   //      1:  _t1 := primitive<==>(_t0, 1.0)
//         false,   //      2:  if not _t1 goto L0
//        
//         true,    //      3:  _t2 := FALSE
//         false,   //      4:  goto L1
//         
//         true,    // L0   5:  y := 0.0
//         false,   //      6:  _t5 := dynamic<seq_along>(x)
//         false,   //      7:  _t3 := 0
//         false,   //      8:  _t4 := primitive<length>(_t5)
//         false,   //      9:  goto L3
//         
//         true,    //L2   10: i := _t5[_t3]
//         false,   //     11: y := primitive<+>(y, 1.0)
//         false,   //L4   12: increment counter _t3
//         
//         true,    //L3   13: if not _t3 >= _t4 goto L2
//         
//         true,    //L5   14: _t2 := NULL
//         
//         true    //L1   15: return _t2
//      }));
//    
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
    System.out.println(cfg);
  }

  @Test
  public void cytron() throws IOException {
    IRBody block = parseCytron();
    System.out.println(block);

    ControlFlowGraph cfg = new ControlFlowGraph(block);
    List<BasicBlock> bb = cfg.getLiveBasicBlocks();

    System.out.println(cfg);
    cfg.dumpGraph();
    
    // see Figure 5 in 
    // http://www.cs.utexas.edu/~pingali/CS380C/2010/papers/ssaCytron.pdf

    assertThat(cfg.getSuccessors(bb.get(0)), itemsEqualTo(bb.get(1), cfg.getExit()));
    assertThat(cfg.getSuccessors(bb.get(1)), itemsEqualTo(bb.get(2)));
    assertThat(cfg.getSuccessors(bb.get(2)), itemsEqualTo(bb.get(3), bb.get(7)));
    assertThat(cfg.getSuccessors(bb.get(3)), itemsEqualTo(bb.get(4), bb.get(5)));
    assertThat(cfg.getSuccessors(bb.get(4)), itemsEqualTo(bb.get(6)));
    assertThat(cfg.getSuccessors(bb.get(5)), itemsEqualTo(bb.get(6)));
    assertThat(cfg.getSuccessors(bb.get(6)), itemsEqualTo(bb.get(8)));
    assertThat(cfg.getSuccessors(bb.get(7)), itemsEqualTo(bb.get(8)));
    assertThat(cfg.getSuccessors(bb.get(8)), itemsEqualTo(bb.get(9)));
    assertThat(cfg.getSuccessors(bb.get(9)), itemsEqualTo(bb.get(10), bb.get(11)));
    assertThat(cfg.getSuccessors(bb.get(10)), itemsEqualTo(bb.get(11)));
    assertThat(cfg.getSuccessors(bb.get(11)), itemsEqualTo(bb.get(9),bb.get(12)));
    assertThat(cfg.getSuccessors(bb.get(12)), itemsEqualTo(bb.get(13),bb.get(2)));
  }

  
}
