import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class VectorPipelinerTest extends EvalTestCase {

  @Test
  public void test() {
    eval("x <- .Internal(runif(1e6, 0, 1))");
    eval("y <- sqrt(x + 1)");
    eval("z <- mean(y) - mean(x)");
    eval("attr(z, 'comments') <- 'still not actually computed'  ");
    eval("print(length(z))");
    eval("print(z)");
  }



}
