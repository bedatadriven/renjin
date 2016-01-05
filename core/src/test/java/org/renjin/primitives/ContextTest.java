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

package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class ContextTest extends EvalTestCase {

  @Test
  public void nframesGlobal() {
    assertThat( eval(".Internal(sys.nframe())"), equalTo( c_i(0)));
  }

  @Test
  public void nframesInClosure() {
    eval( "sys.nframe <- function() .Internal(sys.nframe()) ");
    eval( "f <- function() { sys.nframe() }");
    eval( "g <- function() f() ");
    eval( "h <- function() g() ");

    assertThat( eval(" h() "), equalTo( c_i(3) ));
  }


  @Test
  public void nframesInClosureWithS3() {
    eval( "sys.nframe <- function() .Internal(sys.nframe()) ");

    eval( "g.default <- function(x) sys.nframe()  ");
    eval( "g <- function(x) UseMethod('g')" );
    eval( "h <- function(x) g(x) ");

    assertThat( eval(" h(0) "), equalTo( c_i(3) ));
  }


  @Test
  public void sysFrames() {
    eval(" sys.frame <- function (which = 0) .Internal(sys.frame(which))");
    eval(" f <- function() sys.frame(-1)");
    eval(" g <- function() sys.frame(-1)$z ");
    eval(" h <- function() { z<-99; g() } ");

    assertThat(eval("sys.frame(0)"), is((SEXP) global));
    assertThat(eval("f()"), is((SEXP) global));
    assertThat( eval("h()"), equalTo(c(99)));
  }

  @Test
  public void parentFrameInGlobal() {
    eval( "parent.frame <- function(n = 1) .Internal(parent.frame(n)) ");
    eval( "environment <- function(fun = NULL) .Internal(environment(fun))  ");
    eval( "f <- function() parent.frame()");
    eval( "g <- function() c( f(), environment() ) ");
    eval( "gx <- g()");
    
    assertThat(eval("parent.frame()"), is(GlobalEnv));
    assertThat(eval("parent.frame(1)"), is(GlobalEnv));
    assertThat(eval("parent.frame(99)"), is(GlobalEnv));
    assertThat(eval("gx[[1]]"), is(eval("gx[[2]]")));
  }

  @Test
  public void parentFrameInGlobal2() {
    eval( "parent.frame <- function(n = 1) .Internal(parent.frame(n)) ");
    eval("f <- function() parent.frame(2)$xx");
    eval("g <- function() f() ");
    eval("h <- function() { xx <- 99; g() } ");
    
    assertThat(eval("h()"), equalTo(c(99)));
  }
  
  
  @Test(expected = EvalException.class)
  public void parentFrameInvalidArg() {
    eval(" .Internal(parent.frame(-1)) ");
  }
  
  @Test
  public void parentFrameClosure() {
    eval(" parent.frame <- function(n=1) .Internal(parent.frame(n)) ");
    eval(" g <- function() { parent.frame()$zz + 1 } ");
    eval(" f <- function() { zz<-41; g() } ");

    assertThat(eval("f()"), equalTo(c(42)));
  }

  @Test
  public void parentFrameInFormals() {

    eval(" parent.frame <- function(n=1) .Internal(parent.frame(n)) ");
    eval(" g<- function(env = parent.frame()) env$zzz");
    eval(" f<- function() { zzz<-42; g() } ");

    assertThat( eval("f()"), equalTo( c(42)));
  }


  
  @Test
  public void sysCall() {
    eval(" sys.call <- function (which = 0) .Internal(sys.call(which))");

    eval(" g <- function(x) sys.call(0) ");

    FunctionCall call = (FunctionCall)eval("g(1)");
    assertThat(call.getFunction(), equalTo(symbol("g")));
  }

  @Test
  public void sysParent() {
    eval(" sys.parent <- function (n = 1) .Internal(sys.parent(n))");
    eval(" f <- function() sys.parent() ");
    eval(" g <- function() f() ");

    assertThat(eval("f()"), equalTo(c_i(0)));
    assertThat( eval("g()"), equalTo( c_i(1) ));
  }

  @Test
  public void contextPromises() {
    eval(" sys.frame <- function(which = 0L) .Internal(sys.frame(which)) ");
    eval(" function (generic = NULL, object = NULL, ...) .Internal(NextMethod(generic, object, ...))");
    eval(" f <- function() sys.frame(-1)$q ");
    eval(" g <- function() { q<-42; f(); }");

    assertThat(eval("g()"), equalTo(c(42)));

    eval(" h <- function(x) { q<- 41; x }");
    assertThat(eval("h(f())"), equalTo(c(41)));
  }

  @Test
  public void primitiveDispatchInPromise() {
    eval(" NextMethod <- function (generic = NULL, object = NULL, ...) .Internal(NextMethod(generic, object, ...))");

    eval(" toupper <- function(x) .Internal(toupper(x)) ");
    eval(" as.character.foo <- function(x) toupper(NextMethod('as.character'))");
    eval(" x <- 'bar' ");
    eval(" class(x) <- 'foo'");

    assertThat( eval("as.character(x) "), equalTo(c("BAR")));
  }


  @Test
  public void moreContextsPromises() {
    eval(" sys.frame <- function(which = 0L) .Internal(sys.frame(which)) ");

    eval("MyNextMethod <- function() sys.frame(-1)$.Class");
    eval("`[.foo` <- function(x, i) list(MyNextMethod())");

    eval(" x <- c(1,2,3) ");
    eval(" class(x) <- 'foo' ");
    
    // because 'list' is a primitive, it's arguments are evaluated in the same context
    // as 'f's body, and sys.frame works logically
    assertThat(eval("x[1]"), equalTo(list("foo")));

    eval("myClosure <- function(x) x");
    eval("`[.foo` <- function(x, i) myClosure(MyNextMethod())");

    // now MyNextMethod() is evaluated in the new context for myClosure and
    // sys.frame(-1) refers to myClosure's environment.
    assertThat(eval("x[1]"), equalTo(NULL));
  }

  @Test
  public void sysCalls() {
    eval("g <- function(x) sys.calls() ");
    eval("f <- function(x) g(x)");
    PairList calls = (PairList) eval("x <- f(1)");
    
    assertThat(calls.length(), equalTo(2));
  }


}


