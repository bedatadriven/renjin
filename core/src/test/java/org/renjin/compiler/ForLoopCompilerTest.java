package org.renjin.compiler;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ForLoopCompilerTest extends EvalTestCase {

  @Test
  @Ignore("only for demo purposes")
  public void simpleLoopDemo() throws IOException {

    ExpressionVector bodySexp = RParser.parseSource(
        Resources.toString(Resources.getResource(ForLoopCompilerTest.class, "simpleLoop.R"), Charsets.UTF_8));

    Session session = new SessionBuilder().build();
    session.getTopLevelContext().evaluate(bodySexp);
  }

  @Test
  public void simpleLoop() throws IOException {
    assertThat(eval("{ s <- 0; for(i in 1:10000) { s <- s + sqrt(i) }; s }"), closeTo(c(666716.5), 1d));
  }

  @Test
  public void loopWithS3Call() {
    
    // The + operator is overloaded with a `+.foo` method for class 'foo'
    // We should either bailout or specialize to the provided function
    
    eval("  `+.foo` <- function(x, y) structure(42, class='foo') ");
    eval(" s <- structure(1, class='foo') ");
    eval(" for(i in 1:500) s <- s + sqrt(i) ");
      
    assertThat(eval("s"), equalTo(c(42)));
  }

  @Test
  public void attributePropagation() {
    
    eval(" s <- structure(1, foo='bar') ");
    eval(" for(i in 1:500) s <- s + sqrt(i) ");

    assertThat(eval(" s "), closeTo(c(7465.534), 0.01));
    assertThat(eval(" attr(s, 'foo') "), equalTo(c("bar")));
  }

  @Test
  public void testVectorBuild() throws IOException {
    eval("x <- numeric(10000); for(i in seq_along(x)) { y <- x; x[i] <- sqrt(i) }"); 
  }


}
