package r.compiler.cfg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import r.compiler.ir.tac.IRBlock;
import r.compiler.ir.tac.IRBlockBuilder;
import r.compiler.ir.tac.IRFunctionTable;
import r.lang.ExpressionVector;
import r.parser.RParser;

public class BasicBlockAlgorithmTest {

  @Test
  public void singleBlock() {
    IRBlock block = buildBody("y<-x+1;z<-3; 4");
    boolean leaders[] = BasicBlockAlgorithm.markLeaders(block);

    assertThat(leaders, equalTo(new boolean[]{true,false,false}));
    
    List<BasicBlock> basicBlocks = BasicBlockAlgorithm.basicBlocks(block);
    assertThat(basicBlocks.size(), equalTo(1));
    assertThat(basicBlocks.get(0).size(), equalTo(block.getStatements().size()));
        
  }
  
  @Test
  public void forBlock() {
    IRBlock block = buildBody("if(length(x)==1) FALSE else { y<-0; for(i in seq_along(x)) y <- y+1 }");
    System.out.println(block);
    
    boolean leaders[] = BasicBlockAlgorithm.markLeaders(block); 

    assertThat(leaders, equalTo(new boolean[]{    
         true,    //      0:  _t0 := dynamic<length>(x)
         false,   //      1:  _t1 := primitive<==>(_t0, 1.0)
         false,   //      2:  if not _t1 goto L0
        
         true,    //      3:  _t2 := FALSE
         false,   //      4:  goto L1
         
         true,    // L0   5:  y := 0.0
         false,   //      6:  _t5 := dynamic<seq_along>(x)
         false,   //      7:  _t3 := 0
         false,   //      8:  _t4 := primitive<length>(_t5)
         false,   //      9:  goto L3
         
         true,    //L2   10: i := _t5[_t3]
         false,   //     11: y := primitive<+>(y, 1.0)
         false,   //L4   12: increment counter _t3
         
         true,    //L3   13: if not _t3 >= _t4 goto L2
         
         true,    //L5   14: _t2 := NULL
         
         true    //L1   15: return _t2
      }));
    
    ControlFlowGraph cfg = new ControlFlowGraph(block);
    
    System.out.println(cfg);
    
  }

  private IRBlock buildBody(String rcode) {
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    IRFunctionTable functionTable = new IRFunctionTable();
    return new IRBlockBuilder(functionTable).build(ast);
  }

}
