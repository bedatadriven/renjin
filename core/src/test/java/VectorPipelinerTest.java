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


  @Test
  public void logicalOperator2() {
    // a: >([400d], 0.2)
    // b: <([400d], 0.2)
    // c: +(b, a)
    // d: *(1.0, [400d])
    eval("x <- double(400)");
    eval("a <- x > 0.2");
    eval("b <- x < 0.2");
    eval("c <- a + b");
    eval("d <- 1.0 * x");
    eval("e <- c * d");

    eval("x <- sum(e)");
    eval("print(x)");
  }



}
