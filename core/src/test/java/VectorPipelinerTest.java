/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.SEXP;


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
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "x", new LogicalArrayVector(new int[1000]));
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "y", new DoubleArrayVector(new double[1000]));

    eval("a <- !x");
    eval("b <- y * a");
    eval("c <- y * x");
    eval("d <- b + c");
    eval("e <- y * d");
    eval("f <- e * y");
    eval("g <- sum(f)");
    
    eval("print(g)");
  }

  @Test
  public void crossProduct() {

    //crossprod(+(-([600]), *(1200.0, [600])))

    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "x", new DoubleArrayVector(new double[600]));
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "y", new DoubleArrayVector(new double[600]));


    eval("z <- crossprod(-x + (1200 * y))");

    SEXP z = topLevelContext.materialize(eval("z"));

  }



}
