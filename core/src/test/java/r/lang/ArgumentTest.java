/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

import org.junit.Test;
import r.lang.exception.EvalException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArgumentTest extends EvalTestCase {

  @Test
  public void singlePosArg() {
    eval( "f <- function(x) { x } ");
    assertThat(eval("f(42)"), equalTo(c(42)));
  }

  @Test
  public void singlePosArgWithDefault() {
    eval( "f <- function(x = 99) { x } ");
    assertThat(eval("f()"), equalTo(c(99)));
  }

  @Test
  public void singleNamedArg() {
    eval( "f <- function(x) { x } ");
    assertThat(eval("f(x=241)"), equalTo(c(241)));
  }

  @Test
  public void partialMatching() {
    eval( "f <- function(reallyLongArgName) { reallyLongArgName } ");
    assertThat(eval("f(r=11)"), equalTo(c(11)));
  }

  @Test
  public void partialAndExactMatching() {
    // Notice that if f <- function(fumble, fooey) fbody, then f(f = 1, fo = 2) is illegal,
    // even though the 2nd actual argument only matches fooey. f(f = 1, fooey = 2) is legal
    //  though since the second argument matches exactly and is removed from consideration for
     // partial matching.

    eval( "f <- function(fumble, fooey) { fumble ^ fooey } ");
    assertThat( eval( "f(f = 3, fooey = 4)"), equalTo( c(81)  ) );
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

  @Test
  public void varArgs() {
    eval( "f <- function(...) { list(...) }");
    assertThat( eval( "f(1,2,3) "), equalTo( list(1d,2d,3d) ));
  }

  @Test
  public void taggedVarArgs() {
    eval( "f <- function(...) { list(...) }");
    ListExp exp = (ListExp) eval( "f(x=1,y=2,z=3)");
    assertThat(exp.getName(0), equalTo("x"));
    assertThat(exp.getName(1), equalTo("y"));
    assertThat(exp.getName(2), equalTo("z"));

  }

  @Test
  public void varArgWithNamed() {
    eval( "f <- function(..., x) length(list(...))");
    assertThat( eval(" f(x=99, 1, 2, 3) "), equalTo( c_i(3) ));
  }

  @Test
  public void varArgWithExtraNamed() {
    eval( "f <- function(...) { length(list(...)) } ");
    assertThat( eval(" f(x=32, y=42, z=99) "), equalTo( c_i(3) ));
  }

  @Test
  public void ellipsesPassedToClosure() {
    eval( "g<- function(a,b,c) { list(a,b,c) } ");
    eval( "f<- function(...) { g(...) }");
    assertThat( eval(" f(1,2,3) "), equalTo(list(1d,2d,3d)));
  }

@Test
  public void ellipsesPassedToClosureWithTags() {
    eval( "g<- function(...) { list(...) } ");
    eval( "f<- function(...) { g(...) }");
    ListExp exp = (ListExp) eval(" f(x=1,y=2,z=3) ");
    assertThat(exp.getName(0), equalTo("x"));
    assertThat(exp.getName(1), equalTo("y"));
    assertThat(exp.getName(2), equalTo("z"));
  }

  @Test
  public void evaluatedPromise() {
    eval( "x <- 1 " );
    eval( "f <- function(a = x) { a } ");
    assertThat( eval(" f()" ), equalTo( c(1))) ;
  }

  @Test
  public void argsArePromisedInCorrectEnv() {
    eval( "f <- function(x) { x } ");
    eval( "x <- 1 ");
    assertThat( eval(" f(x) "), equalTo( c(1) ));
  }

  @Test
  public void argsArePromisedInCorrectEnv2() {
    eval( "f <- function(x, y = x) { y } ");
    eval( "x <- 2 ");
    eval( "y <- 3 ");
    assertThat( eval(" f(y) "), equalTo( c(3) ));

  }


  @Test
  public void promiseEvaluatedInFunctionEnv() {
    eval( "f <- function( a = sqrt(y) ) { y<-4; a } ");
    assertThat( eval("f()"), equalTo( c(2) ));
  }

  @Test
  public void argsPromisedInCallingEnv() {
    eval( "g <- function(z) { z } ");
    eval( "f <- function() { q<-3; g(q) }");

    assertThat( eval("f()"), equalTo( c(3) ));
  }

  @Test
  public void autoPrintingFun1() {

    eval( "f <- function( a = if(FALSE) {1 } ) { a }" );

    assertThat(evaluate( "f()").isVisible(), equalTo(false)) ;
    assertThat(evaluate( "f()").getExpression(), equalTo( (SEXP)NullExp.INSTANCE )) ;
  }

  @Test
  public void autoPrintingFun2() {

    // this fails in R-2.1.0 but I think this behavior is more consistent!
    eval( "f <- function( a = if(FALSE) {1 } ) { a; a}" );

    assertThat(evaluate( "f()").isVisible(), equalTo(false)) ;
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
    
      assertThat( eval( "f(1,2,3)"), equalTo( c(1,2,3 )));
  }



}
