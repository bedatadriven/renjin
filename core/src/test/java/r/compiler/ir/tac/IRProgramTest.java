package r.compiler.ir.tac;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.junit.Test;

import r.compiler.ir.ProgramCompiler;
import r.compiler.ir.optimize.StaticOptimizer;
import r.compiler.ir.tree.TreeBuilder;
import r.lang.SEXP;
import r.parser.RParser;

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
