package org.renjin.compiler.ir.tac;

import org.junit.Test;
import org.renjin.compiler.ir.ProgramCompiler;
import org.renjin.parser.RParser;
import org.renjin.sexp.CHARSEXP;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.InputStreamReader;


public class IRProgramTest {

  @Test
  public void meanOnline() throws IOException {
    
    String resourceName = "/meanVarOnline.R";

    SEXP programExpression = RParser.parseSource(new InputStreamReader(getClass()
        .getResourceAsStream(resourceName)), new CHARSEXP("class://"+resourceName));
    
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
