import org.junit.Test;
import org.renjin.EvalTestCase;


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

  @Test
  public void logicalOperator() {
    // Binary operator with integer result, used as a double
    eval("x <- sum( (1:10000 < 500) + 1)");
    eval("print(x)");
  }


}
