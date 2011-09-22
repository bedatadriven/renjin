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

package r.base;

import org.junit.Test;
import r.EvalTestCase;
import r.lang.*;
import r.lang.exception.EvalException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.ExpMatchers.logicalVectorOf;
import static r.ExpMatchers.realVectorEqualTo;

public class EvaluationTest extends EvalTestCase {

  @Test
  public void unaryFunction() throws IOException {
    SEXP result = eval("sqrt(4)");

    assertThat(result, realVectorEqualTo(2));
  }

  @Test
  public void ifStatement() throws IOException {
    assertThat(eval("if(TRUE) 1"), realVectorEqualTo(1));
  }

  @Test
  public void ifStatementWithArgsToBeEvaluated() throws IOException {
    evaluate("x<-1");
    assertThat(eval("if(x) 9"), realVectorEqualTo(9));
  }

  @Test
  public void ifElseStatement() throws IOException {
    assertThat(eval("if(TRUE) 1 else 2"), realVectorEqualTo(1));
  }

  @Test
  public void ifElseFalseStatement() throws IOException {
    assertThat(eval("if(FALSE) 1 else 2"), realVectorEqualTo(2));
  }

  @Test(expected = Exception.class)
  public void ifWithNA() throws IOException {
    eval("if(NA) 1");
  }

  @Test
  public void braces() throws IOException {
    assertThat(eval("{1; 2}"), realVectorEqualTo(2));
  }

  @Test
  public void emptyBraces() throws IOException {
    assertThat(eval("{}"), equalTo((SEXP) Null.INSTANCE));
  }

  @Test
  public void assign() throws IOException {
    assertThat(eval("x<-2"), realVectorEqualTo(2));
    assertThat(eval("x"), realVectorEqualTo(2));
  }

  @Test
  public void assignPrecedence() {
    eval("x<-1");
    eval("f<-function(z) { if(z!=1) stop('expected z==1'); 42 } ");
    eval("x<-f(x)");
  }

  @Test
  public void assignIsSilent() throws IOException {
    assertThat(evaluate("x<-1").isVisible(), equalTo(false));
  }

  @Test
  public void assignSym() throws IOException {
    eval("x<-1");
    eval("y<-x");
    assertThat(eval("y"), realVectorEqualTo(1));
  }

  @Test
  public void whileLoop() throws IOException {
    eval("x<-TRUE");
    eval("while(x) { x<-FALSE }");

    assertThat(eval("x"), logicalVectorOf(Logical.FALSE));
  }

  @Test
  public void whileLoopWithBreak() throws IOException {
    eval("x<-TRUE");
    eval("while(x) { break; x<-FALSE }");

    assertThat(eval("x"), logicalVectorOf(Logical.TRUE));
  }

  @Test
  public void repeatLoop() {
    eval("y<-0");
    eval("repeat { y <- y + 1; if(y > 5) break }");
    
    assertThat( eval("y"), equalTo(c(6)));
    
  }
  
  @Test
  public void whileLoopWithNext() throws IOException {
    eval("x<-1");
    eval("y<-0");
    eval("while(x<5) {  x<-x+1; if(x==3) next; y<-y+1 }");

    assertThat(eval("y"), equalTo( c(3) ));
  }

  @Test
  public void simplestForStatement() throws IOException {
    eval("for( x in 99 ) { y <- x} ");

    assertThat(eval("x"), realVectorEqualTo(99));
    assertThat(eval("y"), realVectorEqualTo(99));
  }

  @Test
  public void function() throws IOException {
    eval("f <- function(x) { x }");
    assertThat(eval("f(4)"), realVectorEqualTo(4));
  }

  @Test
  public void functionWithMissing() throws IOException {
    eval("f <- function(x) { missing(x) }");
    assertThat(eval("f()"), logicalVectorOf(Logical.TRUE));
    assertThat(eval("f(1)"), logicalVectorOf(Logical.FALSE));
  }


  @Test
  public void missingWithDefaultArg() {
    eval("f<-function(x=1) missing(x) ");

    assertThat( eval("f()"), equalTo( c(true)));
  }

  @Test
  public void missingWithNullDefaultAndGenerics() {
    eval("f.default<-function(formula, data=NULL) missing(data) ");
    eval("f <- function(formula, ...) UseMethod('f'); ");

    eval("data <-88");
    assertThat( eval("f(1, data=data)"), equalTo( c(false)));
  }

  @Test
  public void missingWithDefaultArgPart2() {
    eval("y <- 4");
    eval("f<-function(x=1){  if(!missing(x)) 41 else 42 } ");

    assertThat( eval("f(y)"), equalTo( c(41)));
  }

  @Test
  public void functionWithZeroArgs() throws IOException {
    eval("f <- function() { 1 } ");
    assertThat(eval("f()"), realVectorEqualTo(1));
  }

  @Test
  public void onExit() {

    eval(" f<-function() { on.exit( .Internal(eval(quote(launchMissiles<-42), globalenv(), NULL))) }");
    eval(" f() ");

    assertThat( eval(" launchMissiles "), equalTo( c(42) ) );
  }

  @Test
  public void onExitCorrectEnvironment() {
    eval(" f<- function() { tutty.fruity <- 3; on.exit(tutty.fruity+1) }");
    eval(" f() ");

  }

  @Test
  public void globalAssign() {

    eval("myf <- function(x) { " +
        " innerf <- function(x) .Internal(assign(\"Global.res\", x^2, globalenv(), FALSE)); " +
        " innerf(x+1) " +
        "}");
    eval("myf(3)");

    assertThat( eval("Global.res"), equalTo( c(16) ));
  }

  @Test
  public void complexAssignment() {
    eval( " x <- list(a = 1)");
    eval( " x$a <- 3");

    assertThat( eval("x$a"), equalTo( c(3)));
  }

  @Test
  public void complexAssignmentWithClass() {
    eval( " x<- list(a = 1)");
    eval( " class(x$a) <- 'foo' ");

    assertThat( eval(" x$a "), equalTo( c(1) ));
    assertThat( eval(" class(x$a) "), equalTo( c("foo")));
  }

  @Test
  public void complexAssignmentWithSubset() {
    eval( " x <- list( a = c(91,92,93) ) ");
    eval( " x$a[3] <- 42");
  }

  @Test
  public void chainedComplexAssignment() {
    eval( "x <- y <- z <- 1");

    assertThat( eval("x"), equalTo(c(1)));
    assertThat( eval("y"), equalTo(c(1)));
    assertThat( eval("z"), equalTo(c(1)));

    eval(" class(x) <- class(y) <- class(z) <- 'foo'");

    assertThat( eval("class(x)"), equalTo(c("foo")));
    assertThat( eval("class(y)"), equalTo(c("foo")));
    assertThat( eval("class(z)"), equalTo(c("foo")));
  }

  @Test
  public void functionLookup() {

    eval("f<-function(x) x");
    eval("g<-function() { f<-3; f(f); }");

    assertThat( eval(" g() "), equalTo(c(3)));

  }
  
  @Test
  public void dotDotDotToPrimitive() {
    eval("f<-function(...) sqrt(...) ");
    assertThat( eval("f(4)"), equalTo(c(2)));
  }

  @Test
  public void substitute() {
    eval(" f1 <- function(x, y = x)             { x <- x + 1; y }   ");
    eval(" s1 <- function(x, y = substitute(x)) { x <- x + 1; y }   ");
    eval(" s2 <- function(x, y) { if(missing(y)) y <- substitute(x); x <- x + 1; y } ");
    eval(" a <- 10  ");

    assertThat( eval(" f1(a) "), equalTo( c(11) ) );
    assertThat( eval(" s1(a) "), equalTo( c(11) ) );
    assertThat( eval(" s2(a) "), equalTo( symbol("a") ));
  }

  @Test
  public void substituteDotDot() {
    eval(" f<- function(...) substitute(list(...)) ");

    assertThat( eval("f(a,b)"), equalTo( (SEXP) new FunctionCall(new Symbol("list"),
        PairList.Node.fromArray(new Symbol("a"), new Symbol("b"))) ));
  }

  @Test
  public void returnInPromises() {

    eval(" f <- function() { " +
              "g <- function(expr) expr ; " +
              "g(return(42)) ; " +
              "return(-1) " +
        "}");


    assertThat( eval("f()"), equalTo(c(42)));
  }

  @Test
  public void quoteSymbol() {
    eval("x <- quote(y)");
    assertThat( eval("x"), equalTo(symbol("y")));
  }

  @Test
  public void symbolToCharacter() {
    assertThat( eval(" as.character(quote(x)) "), equalTo( c("x")));
  }

  @Test
  public void doSwitch() {
    eval("switch <- function (EXPR, ...) .Internal(switch(EXPR, ...))");

    assertThat( eval("switch('z', alligator=4,aardvark=2, 44)"), equalTo( c(44)));
    assertThat( eval("switch('a', alligator=4,aardvark=2, 44)"), equalTo( c(44)));
    assertThat( eval("switch('a', alligator=4,aardvark=2)"), equalTo( NULL ));
    assertThat( eval("switch('all', alligator=4,aardvark=2)"), equalTo( c(4) ));
    assertThat( eval("switch('all')"), equalTo( NULL ));

    assertThat( eval("switch(1, 'first', 'second')"), equalTo( c("first") ));
    assertThat( eval("switch(2, 'first', 'second')"), equalTo( c("second") ));
    assertThat( eval("switch(99, 'first', 'second')"), equalTo( NULL ));
    assertThat( eval("switch(4)"), equalTo( NULL ));

    assertThat( eval("switch('a', a=,b=,c=3) "), equalTo( c(3)));
  }

  @Test
  public void useMethod() {
    eval("fry <- function(what, howlong) UseMethod('fry') ");
    eval("fry.default <- function(what, howlong) list(desc='fried stuff',what=what,howlong=howlong) ");
    eval("fry.numeric <- function(what, howlong) list(desc='fried numbers',number=what,howlong=howlong)" );

    eval("x<-33");
    eval("class(x) <- 'foo'");

    assertThat( eval("fry(1,5)"), equalTo( eval("list(desc='fried numbers', what=1, howlong=5)") ) );
    assertThat( eval("fry(x,15)"), equalTo( eval("list(desc='fried stuff', what=33, howlong=15)") ) );

    eval("cook <- function() { eggs<-6; fry(eggs, 5) }");

    assertThat( eval("cook()"), equalTo( eval("list(desc='fried numbers', what=6, howlong=5) ")));
  }
  
  @Test(expected=EvalException.class)
  public void useMethodFailsOnMissingMethod() {
    eval("f <- function(x) UseMethod('f')");
    eval("f.foo <- function(x) 'matrix' ");
    eval("f(9)");
  }
  
  @Test
  public void useMethodDispatchesToMatrices() {
    eval("f <- function(x) UseMethod('f')");
    eval("f.matrix <- function(x) 'matrix' ");
    
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
   
    assertThat(eval("f(m)"), equalTo(c("matrix")));
  }
  
  @Test
  public void useMethodDispatchesToDoubleThenNumeric() {
    eval("f <- function(x) UseMethod('f')");
    eval("f.numeric <- function(x) 'numeric' ");
    eval("f.double <- function(x) 'double' ");
    
   
    assertThat(eval("f(9)"), equalTo(c("double")));
  }
  
  

  @Test
  public void nargs() {
    eval("test <- function(a, b = 3, ...) {nargs()}");

    assertThat( eval("test()"), equalTo( c_i(0)) );
    assertThat( eval("test(clicketyclack)"), equalTo( c_i(1)));
    assertThat( eval("test(c1, a2, rr3)"), equalTo( c_i(3)));
  }

  @Test
  public void delayedAssign() {

    eval("parent.frame <- function (n = 1) " +
        ".Internal(parent.frame(n))");
    eval(" delayedAssign <- function (x, value, eval.env = parent.frame(1), assign.env = parent.frame(1)) " +
             ".Internal(delayedAssign(x, substitute(value), eval.env, assign.env)) ");

    eval(" delayedAssign('x', f(y)) ");
    eval(" y<-3");
    eval(" f<-function(x) x^2 ");

    assertThat( eval("x"), equalTo(c(9)));
  }

  @Test
  public void call() {

    eval(" x <- 0 ");
    eval(" f <- function(value) x<<-value ");
    eval(" call('f', 3) ");

    assertThat( eval("x"), equalTo(c(3)));
  }

  @Test
  public void evalWithPairList() {
    eval(" params <- list(a=1,b=99)");
    eval(" c<-25");
    assertThat( eval( ".Internal(eval(quote((a+b)/c), params, globalenv()))") , equalTo(c(4)));
  }

  @Test
  public void rhsIsEvaledOnlyOnce() {
    eval(" onlyonce <- function() { " +
          		"if(!is.null(globalenv()$once)) stop(); " +
          		" .Internal(assign('once', 1, globalenv(), FALSE));" +
          		" 16 }");
    eval(" k <- list(1,2,3) ");
    eval(" k[[2]] <- onlyonce()");

    assertThat( eval("k"), equalTo(list(1d,16d,3d)));

  }

  @Test
  public void intermediateAssignmentTargetsAreNotEvaled() {
    eval(" x<- quote(shouldNotBeEvaled()) ");
    eval(" attr(x, 'foo') <- 'bar' ");
    eval(" environment(x) <- globalenv() ");
    eval(" class(x) <- 'foo' ");
    
  }
  
}
