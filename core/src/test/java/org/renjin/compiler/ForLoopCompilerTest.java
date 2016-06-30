package org.renjin.compiler;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import java.io.IOException;

import static org.junit.Assert.assertThat;

public class ForLoopCompilerTest extends EvalTestCase {

  @Test
  public void simpleLoopDemo() throws IOException {

    ExpressionVector bodySexp = RParser.parseSource(
        Resources.toString(Resources.getResource(ForLoopCompilerTest.class, "simpleLoop.R"), Charsets.UTF_8));

    Session session = new SessionBuilder().build();
    session.getTopLevelContext().evaluate(bodySexp);
  }

  @Test
  public void simpleLoop() throws IOException {
    assertThat(eval("{ s <- 0; for(i in 1:100000) { s <- s + sqrt(i) }; s }"), closeTo(c(21082008.973918), 1d));
  }


  @Test
  public void testVectorBuild() throws IOException {

    eval("x <- numeric(10000); for(i in seq_along(x)) { y <- x; x[i] <- sqrt(i) }"); 
  }


}
