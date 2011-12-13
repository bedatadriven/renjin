package r.compiler.ir.tac;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import r.lang.SEXP;
import r.parser.RParser;

public class IRProgramTest {

  @Ignore("takes too long - should be moved to benchmarks")
  @Test
  public void meanOnline() throws IOException {
    
    SEXP programExpression = RParser.parseSource(new InputStreamReader(getClass().getResourceAsStream("/meanVarOnline.R")));
    
    IRProgram program = new IRProgram(programExpression);
    
    System.out.println(program.toString());
    
    long start = new Date().getTime();
    
    program.evaluate();
    
    long end = new Date().getTime();
    
    System.out.println(end-start);
    
  }
  
  
}
