package org.renjin.compiler.ir.tac;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.junit.Test;
import org.renjin.compiler.ir.ProgramCompiler;
import org.renjin.compiler.ir.optimize.StaticOptimizer;
import org.renjin.compiler.ir.tree.TreeBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.SEXP;


public class IRProgramTest {

  @Test
  public void meanOnline() throws IOException {
    
    SEXP programExpression = RParser.parseSource(new InputStreamReader(getClass()
        .getResourceAsStream("/meanVarOnline.R")));
    
    ProgramCompiler compiler = new ProgramCompiler();
    compiler.compile(programExpression);
    
    
//    long start = new Date().getTime();
//    
//    program.evaluate();
//    
//    long end = new Date().getTime();
//    
//    System.out.println(end-start);
//    
  }
}
