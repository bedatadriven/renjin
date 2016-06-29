package org.renjin.compiler;


import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ExpressionVector;

public class ForLoopCompilerTest {

  @Test
  public void test() {

    Session session = new SessionBuilder().build();
    ExpressionVector bodySexp = RParser.parseSource("s <- 0; z <- 1:1000; for(i in z) { s <- s + sqrt(i) }; print(s);");

    DoubleVector s = (DoubleVector) session.getTopLevelContext().evaluate(bodySexp);

    
    
  }

}
