package org.renjin.compiler;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import java.io.IOException;

public class ForLoopCompilerTest {

  @Test
  public void testScalar() throws IOException {

    ExpressionVector bodySexp = RParser.parseSource(
        Resources.toString(Resources.getResource(ForLoopCompilerTest.class, "simpleLoop.R"), Charsets.UTF_8));

    Session session = new SessionBuilder().build();
    session.getTopLevelContext().evaluate(bodySexp);
  
  }
  
  @Test
  public void testVectorBuild() {

    ExpressionVector bodySexp = RParser.parseSource("x <- numeric(10000); for(i in seq_along(x)) x[i] <- sqrt(i)");
    Session session = new SessionBuilder().build();
    session.getTopLevelContext().evaluate(bodySexp); 
  }


}
