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
package org.renjin;

import org.junit.Before;
import org.junit.Test;
import org.renjin.sexp.Logical;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class S3DispatchTest extends EvalTestCase {

  @Before
  public void setUpInternalWrappers() {
    eval("NextMethod <- function (generic = NULL, object = NULL, ...) .Internal(NextMethod(generic, object, ...))");
  }

  @Test
  public void genericPrimitive() {
    eval(" version <- list(platform='i386-pc', arch='i386', os='mingw32', major='2', minor='10.1') ");
    eval(" class(version) <- 'simple.list' ");
    eval(" `[.simple.list` <- function (x, i, ...) { y<-NextMethod('['); class(y) <- class(x); y }");

    eval(" v2 <- version[c('major', 'minor')]");

    assertThat( eval("class(v2)"), equalTo(c("simple.list")));
  }

  @Test
  public void nextMethodInSeveralContextsDown() {
    eval(" structure <- function(x, class) { class(x) <- class; x } ");
    eval(" `[.simple.list` <- function(x, i, ...) { structure(NextMethod('['), class = class(x)) }");
    eval(" sl <- list(41,42,43) ");
    eval(" class(sl) <- 'simple.list'");

    eval(" x <- sl[1]");
    
    assertThat( eval("class(x)"), equalTo(c("simple.list")));
    assertThat( eval("x"), equalTo(list(41d)));

  }
  
  @Test
  public void groupGeneric() {
    eval(" Ops.numeric_version <- function(e1,e2) { e1<-e1$value; e2<-e2$value; NextMethod(.Generic) } ");
    eval(" o1 <- list(value=4) ");
    eval(" class(o1) <- 'numeric_version'");

    assertThat( eval("o1 < o1"), equalTo(c(Logical.FALSE)));
  }
  
  @Test
  public void argsAreEvaluatedWhenDispatchedFromPrimitive() {
    eval(" `[.foo` <- function(x, i, j) i == 1");
    eval(" x <- 1:9" );
    eval(" class(x) <- 'foo'");
   
    eval(" z <- 1" );
    
    assertThat( eval(" x[z] "), equalTo(c(true))); 
  }
  
  
  @Test
  public void missingInS3Generic() {
    eval("`[.foo` <- function(x, i, j, drop = TRUE) c(missing(i), missing(j), missing(drop)) ");
    eval("x <- 1:5");
    eval("class(x) <- 'foo'");
    
    assertThat(eval("x[1,2]"), equalTo(c(false,false,true)));
    assertThat(eval("x[,2]"), equalTo(c(true,false,true)));
  }
  
  @Test
  public void argumentsGoneMissing() {
    eval("g <- function(x, y, drop=FALSE) .Internal(assign('d', drop, globalenv(), FALSE)) ");
    eval("f.default <- function(x, y, drop=FALSE) g(x,y,drop) ");
    eval("f <- function(x,y,drop=FALSE) UseMethod('f')");
    eval("f(4,5)");
    assertThat(eval("d"), equalTo(c(false)));
  }

   
  @Test
  public void matchCallWithinGeneric() throws IOException {
    assumingBasePackagesLoad();
    
    eval("g <- { f <- function(a,b,...) { UseMethod('f') } ; " +
    		        "f.default <- function(a,b,...) { match.call() }; " +
    		        "f; } ");
    eval("call <- g(1,2)");
    
    assertThat(eval("call[[1]]"), equalTo(symbol("f.default")));
  }
  
  @Test
  public void nextMethodWithElipises() {
    
    eval("x <- 1:10 ");
    eval("class(x) <- 'foo' ");
    
    eval("`[.foo` <- function(...) NextMethod() ");
    
    assertThat( eval("x[9]"), equalTo(c_i(9)));
  }

  @Test
  public void test() {
    eval("`[.svyrep.design`<-function(x, i, j, drop=FALSE) missing(i) ");
    eval(" x <- 1:3");
    eval(" class(x) <- 'svyrep.design' ");
    eval(" x[1,]");

  }

}
