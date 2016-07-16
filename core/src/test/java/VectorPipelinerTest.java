import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.LogicalArrayVector;


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

  @Test
  public void logicalOperator3() {

    eval("x <- (1:1000) + 1L");
    eval("y <- x < (2L + x)");
    
    eval("print(sum(y + 3))");
  }
  
  @Test
  public void twiceMaterialized() {
    eval("a <- mean((1:1e6)*3)");

    eval("print(a)");
    eval("print(a)");
    
    eval("y <- a + (1:1e6) ^ 4");
    eval("z <- mean(y)");

    eval("print(z)");
    eval("print(z)");
  }
  
  @Test
  public void logicalArgument() {
    topLevelContext.getGlobalEnvironment().setVariable("x", new LogicalArrayVector(new int[1000]));
    topLevelContext.getGlobalEnvironment().setVariable("y", new DoubleArrayVector(new double[1000]));

    eval("a <- !x");
    eval("b <- y * a");
    eval("c <- y * x");
    eval("d <- b + c");
    eval("e <- y * d");
    eval("f <- e * y");
    eval("g <- sum(f)");
    
    eval("print(g)");
  }



}
