package org.renjin.compiler.cfg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.renjin.compiler.CompilerTestCase;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;


public class ControlFlowGraphTest extends CompilerTestCase {

  @Test
  public void singleBlock() {
    IRBody block = buildScope("y<-x+1;z<-3; 4");
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
    System.out.println(cfg);
    
    List<BasicBlock> basicBlocks = cfg.getBasicBlocks();
    assertThat(basicBlocks.size(), equalTo(3));  // entry + 1 + exit = 3
    assertThat(basicBlocks.get(1).getStatements().size(), equalTo(block.getStatements().size()));
  }
  
  @Test
  public void forLoop() {
    IRBody block = buildScope("y <- 0; for(i in 1:10) y <- y + i; sqrt(y + 3 * x)");
    System.out.println(block);

    
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    System.out.println(cfg);

    List<BasicBlock> basicBlocks = cfg.getBasicBlocks();
    assertThat(basicBlocks.size(), equalTo(7));
  }
  
  @Test
  public void forBlock() {
    IRBody block = buildScope("if(length(x)==1) FALSE else { y<-0; for(i in seq_along(x)) y <- y+1 }");
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
