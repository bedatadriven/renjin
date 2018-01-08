/**
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
package org.renjin.sexp;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArgumentTest extends EvalTestCase {

  @Test
  public void singlePosArg() {
    eval( "f <- function(x) { x } ");
    assertThat(eval("f(42)"), elementsIdenticalTo(c(42)));
  }

  @Test
  public void singlePosArgWithDefault() {
    eval( "f <- function(x = 99) { x } ");
    assertThat(eval("f()"), elementsIdenticalTo(c(99)));
  }

  @Test
  public void singleNamedArg() {
    eval( "f <- function(x) { x } ");
    assertThat(eval("f(x=241)"), elementsIdenticalTo(c(241)));
  }

  @Test
  public void partialMatching() {
    eval( "f <- function(reallyLongArgName) { reallyLongArgName } ");
    assertThat(eval("f(r=11)"), elementsIdenticalTo(c(11)));
  }

  @Test
  public void partialAndExactMatching() {
    // Notice that if f <- function(fumble, fooey) fbody, then f(f = 1, fo = 2) is illegal,
    // even though the 2nd actual argument only matches fooey. f(f = 1, fooey = 2) is legal
    //  though since the second argument matches exactly and is removed from consideration for
    // partial matching.

    eval( "f <- function(fumble, fooey) { fumble ^ fooey } ");
    assertThat( eval( "f(f = 3, fooey = 4)"), elementsIdenticalTo( c(81)  ) );
  }

  @Test(expected = EvalException.class)
  public void multiplePartialMatches() {
    eval( "f <- function(fumble, fooey) { fumble ^ fooey } ");
    eval( "f(f = 1, f = 2)");
  }

  @Test(expected = EvalException.class)
  public void extraArgs() {
    eval( "f <- function(x) { } ");
    eval( "f(1,2,3) ");
  }

  @Test(expected = EvalException.class)
  public void incorrectEllipses() {
    eval( "f <- function(x) { } ");
    eval( "f(1,...) ");
  }

  @Test
  public void varArgs() {
    eval( "f <- function(...) { list(...) }");
    assertThat( eval( "f(1,2,3) "), elementsIdenticalTo( list(1d,2d,3d) ));
  }

  @Test
  public void taggedVarArgs() {
    eval( "f <- function(...) { list(...) }");
    ListVector exp = (ListVector) eval( "f(x=1,y=2,z=3)");
    assertThat(exp.getName(0), equalTo("x"));
    assertThat(exp.getName(1), equalTo("y"));
    assertThat(exp.getName(2), equalTo("z"));

  }

  @Test
  public void varArgWithNamed() {
    eval( "f <- function(..., x) length(list(...))");
    assertThat( eval(" f(x=99, 1, 2, 3) "), elementsIdenticalTo( c_i(3) ));
  }

  @Test
  public void varArgWithExtraNamed() {
    eval( "f <- function(...) { length(list(...)) } ");
    assertThat( eval(" f(x=32, y=42, z=99) "), elementsIdenticalTo( c_i(3) ));
  }

  @Test
  public void ellipsesPassedToClosure() {
    eval( "g<- function(a,b,c) { list(a,b,c) } ");
    eval( "f<- function(...) { g(...) }");
    assertThat( eval(" f(1,2,3) "), elementsIdenticalTo(list(1d,2d,3d)));
  }

  @Test
  public void ellipsesPassedToClosureWithTags() {
    eval( "g<- function(...) { list(...) } ");
    eval( "f<- function(...) { g(...) }");
    ListVector exp = (ListVector) eval(" f(x=1,y=2,z=3) ");
    assertThat(exp.getName(0), equalTo("x"));
    assertThat(exp.getName(1), equalTo("y"));
    assertThat(exp.getName(2), equalTo("z"));
  }

  @Test
  public void evaluatedPromise() {
    eval( "x <- 1 " );
    eval( "f <- function(a = x) { a } ");
    assertThat( eval(" f()" ), elementsIdenticalTo( c(1))) ;
  }

  @Test
  public void argsArePromisedInCorrectEnv() {
    eval( "f <- function(x) { x } ");
    eval( "x <- 1 ");
    assertThat( eval(" f(x) "), elementsIdenticalTo( c(1) ));
  }

  @Test
  public void argsArePromisedInCorrectEnv2() {
    eval( "f <- function(x, y = x) { y } ");
    eval( "x <- 2 ");
    eval( "y <- 3 ");
    assertThat( eval(" f(y) "), elementsIdenticalTo( c(3) ));

  }


  @Test
  public void promiseEvaluatedInFunctionEnv() {
    eval( "f <- function( a = sqrt(y) ) { y<-4; a } ");
    assertThat( eval("f()"), elementsIdenticalTo( c(2) ));
  }

  @Test
  public void argsPromisedInCallingEnv() {
    eval( "g <- function(z) { z } ");
    eval( "f <- function() { q<-3; g(q) }");

    assertThat( eval("f()"), elementsIdenticalTo( c(3) ));
  }

  @Test
  public void autoPrintingFun1() {

    eval( "f <- function( a = if(FALSE) {1 } ) { a }" );

    assertThat(evaluate( "f()"), identicalTo( (SEXP) Null.INSTANCE )) ;
    assertThat(topLevelContext.getSession().isInvisible(), equalTo(true));

  }

  @Test
  public void autoPrintingFun2() {

    // this fails in R-2.1.0 but I think this behavior is more consistent!
    eval( "f <- function( a = if(FALSE) {1 } ) { a; a}" );

    eval( "f()");
    assertThat(topLevelContext.getSession().isInvisible(), equalTo(false));
  }


  @Test
  public void methodTable() {
    // from base
    eval("new.env <- function (hash=FALSE, parent=parent.frame(), size=29L)\n" +
        "    .Internal(new.env(hash, parent, size))");
    eval("\".__S3MethodsTable__.\" <- new.env(hash = TRUE, parent = baseenv())");
  }

  @Test
  public void dotDotDot() {
    eval(" f <- function(...) { c(...) } ");

    assertThat( eval( "f(1,2,3)"), elementsIdenticalTo( c(1,2,3 )));
  }
}
