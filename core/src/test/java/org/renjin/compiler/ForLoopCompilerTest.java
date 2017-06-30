/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.special.ForFunction;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.io.Resources;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.SEXP;

import java.io.IOException;

import static org.junit.Assert.assertThat;

public class ForLoopCompilerTest extends EvalTestCase {
  
  @Before
  public void enableLoopCompiler() {
    ForFunction.COMPILE_LOOPS = true;
  }
  
  @After
  public void disableLoopCompiler() {
    ForFunction.COMPILE_LOOPS = false;
  }

  private SEXP evalAndAssertCompiled(String expression) {

    ForFunction.FAIL_ON_COMPILATION_ERROR = true;
    try {
      return eval(expression);
    } finally {
      ForFunction.FAIL_ON_COMPILATION_ERROR = false;
    }
  }

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
    assertThat(evalAndAssertCompiled("{ s <- 0; for(i in 1:10000) { s <- s + sqrt(i) }; s }"), closeTo(c(666716.5), 1d));
  }

  @Test
  public void loopWithS3Call() {

    // The + operator is overloaded with a `+.foo` method for class 'foo'
    // We should either bailout or specialize to the provided function
    
    eval("  `+.foo` <- function(x, y) structure(42, class='foo') ");
    eval(" s <- structure(1, class='foo') ");
    eval(" for(i in 1:500) s <- s + sqrt(i) ");
      
    assertThat(eval("s"), elementsIdenticalTo(c(42)));
  }
  
  @Test
  public void loopWithClosureCall() {
    eval(" add <- function(x, y) x + y ");
    eval(" s <- 0 ");

    evalAndAssertCompiled(" for(i in 1:500) s <- add(s, sqrt(i)) ");
  
    assertThat(eval("s"), closeTo(c(7464.534), 2.0));
  }

  @Test
  public void loopWithClosureCubeCall() {
    eval(" myfn <- function(i, z) (i/length(z))^2 ");
    eval(" s <- 0 ");
    eval(" z <- 1:500 ");
    evalAndAssertCompiled(" for(i in z) s <- s + myfn(i, z) ");

    assertThat(eval("s"), closeTo(c(167.167), 0.1));
  }

  @Test
  public void attributePropagation() {
    
    eval(" s <- structure(1, foo='bar') ");
    evalAndAssertCompiled(" for(i in 1:500) s <- s + sqrt(i) ");

    assertThat(eval(" s "), closeTo(c(7465.534), 0.01));
    assertThat(eval(" attr(s, 'foo') "), elementsIdenticalTo(c("bar")));
  }

  @Test
  public void testVectorBuild() throws IOException {
    eval("x <- numeric(10000); for(i in seq_along(x)) { y <- x; x[i] <- sqrt(i) }"); 
  }

  @Test
  public void verifyFunctionRedefinitionIsRespected() throws IOException {
    assertThat(eval("{ s <- 0; for(i in 1:10000) { if(i>100) { sqrt <- sin; }; s <- s + sqrt(i) }; s }"),
        closeTo(c(673.224), 1d));

  }

  @Test
  public void activeBindingForLoopIndex() {

    eval(Joiner.on("\n").join(
        "    j <- 4",
        "    ib <- function(val) {",
        "        if(missing(val)) {",
        "            j <<- j + 1",
        "            j",
        "        } else {",
        "            j <<- val * 2",
        "        }",
        "    }",
        "    makeActiveBinding('i', ib, environment())",
        "    sum <- 0",
        "    for(i in 1:10) {",
        "        sum <- sum + i",
        "    }"));

    assertThat(eval("sum"), elementsIdenticalTo(c(120d)));

  }

  @Test
  public void activeBindingInEnvironment() {

    eval(Joiner.on("\n").join(
        ".q <- 0",
        ".qb <- function(val) {",
          ".q <<- .q + 1  # side effect!!",
          ".q",
        "}",
        "makeActiveBinding('q', .qb, environment())",
        "sum <- 0",
        "for(i in 1:1e6) {",
          "sum <- sum + (i * q)",
        "}"));

    assertThat(eval("sum"), elementsIdenticalTo(c(0x1.280f56bddc2e6p+58)));
  }


  @Test
  public void useMethod() {
    eval("foo <- function(a) UseMethod('foo') ");
    eval("foo.default <- function(a) a * 2 ");
    eval("s <- 0");
    evalAndAssertCompiled("print(system.time({ for(i in 1:1e6) s <- s + foo(i) }))");
    eval("print(s)");

    assertThat(eval("s"), elementsIdenticalTo(c(1.000001e+12)));
  }

  @Test
  public void useMethodDispatchesToDoubleThenNumeric() {
    eval("f <- function(x) UseMethod('f')");
    eval("f.numeric <- function(x) 1 ");


    eval("s <- 0");
    evalAndAssertCompiled("for(i in 1:1e6) s <- s + f(i)");

    assertThat(eval("s"), elementsIdenticalTo(c(1e+06)));


    // Define more specific overflows
    eval("f.double <- function(x) 2 ");
    eval("f.integer <- function(x) 3");


    // Ensure that we dispatch to f.integer()
    eval("s <- 0");
    evalAndAssertCompiled("for(i in 1:1e6) s <- s + f(i)");
    assertThat(eval("s"), elementsIdenticalTo(c(3e+06)));


    // Ensure that we dispatch to f.double()
    eval("s <- 0");
    evalAndAssertCompiled("for(i in as.numeric(1:1e6)) s <- s + f(i)");
    assertThat(eval("s"), elementsIdenticalTo(c(2e+06)));

  }


  @Test
  public void constantConditional() {

    eval("s <- 0");
//    eval("for(i in 1:10000) if(typeof(s) == 'double') s <- s + i else stop('not a number')");
    evalAndAssertCompiled("for(i in 1:10000) s <- s + i");

    assertThat(eval("s"), elementsIdenticalTo(c(5.0005e7)));
  }

}
