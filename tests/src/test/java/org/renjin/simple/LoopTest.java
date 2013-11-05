package org.renjin.simple;

import org.junit.Test;

public class LoopTest extends SimpleTestBase {

  @Test
  public void testLoops()  {
    assertEval("{ x<-210 ; repeat { x <- x + 1 ; break } ; x }", "211.0");
    assertEval("{ x<-1 ; repeat { x <- x + 1 ; if (x > 11) { break } } ; x }", "12.0");
    assertEval("{ x<-1 ; repeat { x <- x + 1 ; if (x <= 11) { next } else { break } ; x <- 1024 } ; x }", "12.0");
    assertEval("{ x<-1 ; while(TRUE) { x <- x + 1 ; if (x > 11) { break } } ; x }", "12.0");
    assertEval("{ x<-1 ; while(x <= 10) { x<-x+1 } ; x }", "11.0");
    assertEval("{ x<-1 ; for(i in 1:10) { x<-x+1 } ; x }", "11.0");
    assertEval("{ for(i in c(1,2)) { x <- i } ; x }", "2.0");

    // factorial
    assertEval("{ f<-function(i) { if (i<=1) {1} else {r<-i; for(j in 2:(i-1)) {r=r*j}; r} }; f(10) }", "3628800.0");
    // Fibonacci
    assertEval("{ f<-function(i) { x<-integer(i); x[1]<-1; x[2]<-1; if (i>2) { for(j in 3:i) { x[j]<-x[j-1]+x[j-2] } }; x[i] } ; f(32) }", "2178309.0");

    assertEval("{ f<-function(r) { x<-0 ; for(i in r) { x<-x+i } ; x } ; f(1:10) ; f(c(1,2,3,4,5)) }", "15.0");
    assertEval("{ f<-function(r) { x<-0 ; for(i in r) { x<-x+i } ; x } ; f(c(1,2,3,4,5)) ; f(1:10) }", "55.0");

    assertEvalError("{ while(1 < NA) { 1 } }", "missing value where TRUE/FALSE needed");
    assertEval("{ l <- quote({for(i in c(1,2)) { x <- i } ; x }) ; f <- function() { eval(l) } ; f() }", "2.0");
    assertEval("{ l <- quote(for(i in s) { x <- i }) ; s <- 1:3 ; eval(l) ; s <- 2:1 ; eval(l) ; x }", "1L");
    assertEval("{ l <- quote({for(i in c(2,1)) { x <- i } ; x }) ; f <- function() { if (FALSE) i <- 2 ; eval(l) } ; f() }", "1.0");
    assertEval("{ l <- quote(for(i in s) { x <- i }) ; s <- 1:3 ; eval(l) ; s <- NULL ; eval(l) ; x }", "3L");

    assertEvalError("{ l <- quote(for(i in s) { x <- i }) ; s <- 1:3 ; eval(l) ; s <- function(){} ; eval(l) ; x }", "invalid for() loop sequence");
    assertEvalError("{ l <- function(s) { for(i in s) { x <- i } ; x } ; l(1:3) ; s <- function(){} ; l(s) ; x }", "invalid for() loop sequence");
    assertEvalError("{ l <- quote({ for(i in s) { x <- i } ; x }) ; f <- function(s) { eval(l) } ; f(1:3) ; s <- function(){} ; f(s) ; x }", "invalid for() loop sequence");

    assertEvalError("{ break; }", "no loop for break/next, jumping to top level");
    assertEvalError("{ next; }", "no loop for break/next, jumping to top level");

    assertEval("{ for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }", "2.0");
    assertEval("{ f <- function() { for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f()  }", "2.0");
    assertEval("{ l <- quote({ for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }) ; f <- function() { eval(l) } ; f()  }", "2.0");
    assertEval("{ l <- quote({ for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }) ; f <- function() { eval(l) } ; f()  }", "2L");
    assertEval("{ f <- function(s) { for(i in s) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f(2:1) ; f(c(1,2,3,4)) }", "2.0");
    assertEval("{ f <- function() { for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f() }", "2L");
    assertEval("{ for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }", "2L");
    assertEval("{ i <- 0L ; while(i < 3L) { i <- i + 1 ; if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }", "2.0");

    assertEval("{ i <- 1 ; r <- NULL ; for(v in list(NA,1)) { r[i] <- typeof(v) ; i <- i + 1 } ; r }", "\"logical\", \"double\"");
  }

  @Test
  public void testDynamic()  {
    assertEval("{ l <- quote({x <- 0 ; for(i in 1:10) { x <- x + i } ; x}) ; f <- function() { eval(l) } ; x <<- 10 ; f() }", "55.0");
  }
}
