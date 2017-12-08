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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;
import org.renjin.primitives.special.IfFunction;
import org.renjin.sexp.*;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.renjin.ExpMatchers.logicalVectorOf;
import static org.renjin.ExpMatchers.realVectorEqualTo;


public class EvaluationTest extends EvalTestCase {

  @Test
  public void unaryFunction() throws IOException {
    SEXP result = eval("sqrt(4)");

    assertThat(result, realVectorEqualTo(2));
  }
  
  @Test
  public void vectorizedSqrt() throws IOException{
    SEXP result = eval("sqrt(c(1,4,9))[2]");
    assertThat(result,realVectorEqualTo(2));
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
  public void ifWithString() {
    assertThat(eval(" if('TRUE') 1 else 0 "), elementsIdenticalTo(c(1)));
    assertThat(eval(" if('FALSE') 1 else 0 "), elementsIdenticalTo(c(0)));
    assertThat(eval(" if('true') 1 else 0 "), elementsIdenticalTo(c(1)));
    assertThat(eval(" if('false') 1 else 0 "), elementsIdenticalTo(c(0)));
    assertThat(eval(" if('T') 1 else 0 "), elementsIdenticalTo(c(1)));
    assertThat(eval(" if('F') 1 else 0 "), elementsIdenticalTo(c(0)));
  }

  @Test
  public void braces() throws IOException {
    assertThat(eval("{1; 2}"), realVectorEqualTo(2));
  }

  @Test
  public void emptyBraces() throws IOException {
    assertThat(eval("{}"), identicalTo(Null.INSTANCE));
  }

  @Test
  public void assign() throws IOException {
    assertThat(eval("x<-2"), elementsIdenticalTo(c(2)));
    assertThat(eval("x"), elementsIdenticalTo(c(2)));
  }
  
  @Test
  public void oldAssign() {
    assertThat(eval("x=2"), elementsIdenticalTo(c(2)));
    assertThat(eval("x"), elementsIdenticalTo(c(2)));
  }

  @Test
  public void assignPrecedence() {
    eval("x<-1");
    eval("f<-function(z) { if(z!=1) stop('expected z==1'); 42 } ");
    eval("x<-f(x)");
  }

  @Test
  public void assignIsSilent() throws IOException {
    eval("x<-1");
    assertThat(topLevelContext.getSession().isInvisible(), equalTo(true));
  }
  
  @Test
  public void invisibleFlagIsReset() throws IOException {
    eval("x<-1");
    eval("x");
    assertThat(topLevelContext.getSession().isInvisible(), equalTo(false));
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
    
    assertThat( eval("y"), elementsIdenticalTo(c(6)));
    
  }
  
  @Test
  public void whileLoopWithNext() throws IOException {
    eval("x<-1");
    eval("y<-0");
    eval("while(x<5) {  x<-x+1; if(x==3) next; y<-y+1 }");

    assertThat(eval("y"), elementsIdenticalTo(c(3)));
  }
  
  @Test
  public void evalOrderArgs() {
    
    eval("f <- function(x = (z + 1)) { mx <- missing(x); z <- 1; x }\n");
    
    assertThat(eval("f()"), elementsIdenticalTo(c(2)));
    
  }

  @Test
  public void simplestForStatement() throws IOException {
    eval("for( x in 99 ) { y <- x} ");

    assertThat(eval("x"), realVectorEqualTo(99));
    assertThat(eval("y"), realVectorEqualTo(99));
  }
  
  @Test
  public void forOverList() {
    eval("alist <- c('a','b','c')");
    eval("for(item in alist) { y<-item } ");
    
    assertThat(eval("y"), elementsIdenticalTo(c("c")));
  }
  
  @Test
  public void function() throws IOException {
    eval("f <- function(x) { x }");
    assertThat(eval("f(4)"), realVectorEqualTo(4));
  }

  @Test
  public void functionWithMissing() throws IOException {
    eval("f <- function(x) { missing(x) }");
    assertThat(eval("f()"), elementsIdenticalTo(c(true)));
    assertThat(eval("f(1)"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void missingArgPropagates() {
    eval("f <- function(x) missing(x) ");
    eval("g <- function(x) f(x) ");
    eval("h <- function(x) g(x) ");
    assertThat(eval("g()"), elementsIdenticalTo(c(true)));
    assertThat(eval("h()"), elementsIdenticalTo(c(true)));

  }

  @Test
  public void missingWithDefaultArg() {
    eval("f<-function(x=1) missing(x) ");

    assertThat( eval("f()"), elementsIdenticalTo( c(true)));
  }
  
  @Test
  public void missingWithDefaultPropagates() {
    eval("f<-function(y, x=1) missing(x) ");
    eval("g<-function(z, ...) f(y=z,...) ");
    
 //   assertThat(eval("g(4)"), elementsIdenticalTo(c(true)));
    assertThat(eval("g(x=4)"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void defaultToMissingIsNotMissing() {
    eval("f<-function(x) { print(x); missing(x) } ");
    eval("g<-function(z=1) f(z) ");

    assertThat( eval("g(4)"), elementsIdenticalTo(c(false)));
  }


  @Test
  public void missingWithNullDefaultAndGenerics() {
    eval("f.default<-function(formula, data=NULL) missing(data) ");
    eval("f <- function(formula, ...) UseMethod('f'); ");

    eval("data <-88");
    assertThat( eval("f(1, data=data)"), elementsIdenticalTo( c(false)));
  }

  @Test
  public void missingWithDefaultArgPart2() {
    eval("y <- 4");
    eval("f<-function(x=1) missing(x) ");

    assertThat( eval("f(y)"), elementsIdenticalTo(c(false)));
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

    assertThat(eval(" launchMissiles "), elementsIdenticalTo(c(42)));
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

    assertThat(eval("Global.res"), elementsIdenticalTo(c(16)));
  }

  @Test
  public void complexAssignment() {
    eval( " x <- list(a = 1)");
    eval( " x$a <- 3");

    assertThat(eval("x$a"), elementsIdenticalTo(c(3)));
  }

  @Test
  public void complexAssignmentFunctionCall() {
    // Should not trigger evaluation...
    eval( " x <- list(call = quote(stop()))");
    eval( " x$call$foo <- 'bar' ");
  }

  @Test
  public void complexReassignment() {
    eval( " x <- list(a = 1)");
    eval( " f<- function() x$a <<- 3 ");
    eval(" f()");

    assertThat( eval("x$a"), elementsIdenticalTo( c(3)));
  }
  
  @Test
  public void complexAssignmentWithClass() {
    eval( " x<- list(a = 1)");
    eval( " class(x$a) <- 'foo' ");

    assertThat(eval(" x$a "), elementsIdenticalTo(c(1)));
    assertThat(eval(" class(x$a) "), elementsIdenticalTo(c("foo")));
  }

  @Test
  public void complexAssignmentWithSubset() {
    eval(" x <- list( a = c(91,92,93) ) ");
    eval(" x$a[3] <- 42");
  }
  
  @Test
  public void complexAssignmentWithElipses() {
    eval("  f<-function(x,f,drop=FALSE) x ");
    eval(" `f<-` <- function(x,f,drop=FALSE,...,value) { .Internal(assign('d', drop, globalenv(), FALSE)); x } ");
    eval("  y <- 3");
    
    eval(" f(y,1:10) <- 4");
    
    assertThat(eval("d"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void chainedComplexAssignment() {
    eval( "x <- y <- z <- 1");

    assertThat( eval("x"), elementsIdenticalTo(c(1)));
    assertThat( eval("y"), elementsIdenticalTo(c(1)));
    assertThat(eval("z"), elementsIdenticalTo(c(1)));

    eval(" class(x) <- class(y) <- class(z) <- 'foo'");

    assertThat(eval("class(x)"), elementsIdenticalTo(c("foo")));
    assertThat(eval("class(y)"), elementsIdenticalTo(c("foo")));
    assertThat(eval("class(z)"), elementsIdenticalTo(c("foo")));
  }

  @Test
  public void functionLookup() {

    eval("f<-function(x) x");
    eval("g<-function() { f<-3; f(f); }");

    assertThat( eval(" g() "), elementsIdenticalTo(c(3)));

  }
 
  
  @Test
  public void dotDotDotToPrimitive() {
    eval("f<-function(...) sqrt(...) ");
    assertThat( eval("f(4)"), elementsIdenticalTo(c(2)));
  }

  @Test
  public void substitute() {
    eval(" f1 <- function(x, y = x)             { x <- x + 1; y }   ");
    eval(" s1 <- function(x, y = substitute(x)) { x <- x + 1; y }   ");
    eval(" s2 <- function(x, y) { if(missing(y)) y <- substitute(x); x <- x + 1; y } ");
   
    eval(" a <- 10  ");

    assertThat( eval(" f1(a) "), elementsIdenticalTo( c(11) ) );
    assertThat(eval(" s1(a) "), elementsIdenticalTo(c(11)));
    assertThat( eval(" s2(a) "), identicalTo( symbol("a") ));
  }
  
  @Test
  public void substituteInGlobalEnvironment() {
    eval(" x <- 42");
    eval(" y <- substitute(x)");
    
    assertThat( eval("y"), identicalTo(Symbol.get("x")));
  }
  
  @Test
  public void substituteArgumentMatchingByName() {
    
    assertThat( eval("substitute(env=.GlobalEnv, expr=x)"), identicalTo(Symbol.get("x")));
    
    eval("f <- function(...) substitute(...) ");
    eval("e <- list(x = 99) ");
    eval("x <- 42");
    
    assertThat( eval("f(x)"), identicalTo(Symbol.get("x")));
    assertThat( eval("f(x, e)"), identicalTo(Symbol.get("x")));
    assertThat( eval("f(x, env=e)"), identicalTo(Symbol.get("x")));
    assertThat( eval("f(env=e, x)"), identicalTo(Symbol.get("e")));

    assertThat( eval("substitute(x, e)"), elementsIdenticalTo(c(99)));
    assertThat( eval("substitute(env=e, x)"), elementsIdenticalTo(c(99)));
    assertThat( eval("substitute(e, expr=x)"), elementsIdenticalTo(c(99)));

    assertThat( eval("f()"), identicalTo(Null.INSTANCE));
    
    assertThat( eval("substitute()"), identicalTo(Symbol.MISSING_ARG));

  }
  
  @Test
  public void substituteWithList() {
    assertThat( eval(" substitute(x, list(x=42)) "), elementsIdenticalTo(c(42)));
  }

  @Test
  public void substituteDotDot() {
    eval(" f<- function(...) substitute(list(...)) ");

    assertThat( eval("f(a,b)"), identicalTo(new FunctionCall(Symbol.get("list"),
        PairList.Node.fromArray(Symbol.get("a"), Symbol.get("b")))));
  }
  
  @Test
  public void substituteWithMissingEllipses() {
    eval(" f<- function(a=1) substitute(list(...)) ");

    assertThat( eval("f()"), identicalTo(new FunctionCall(Symbol.get("list"),
            PairList.Node.fromArray(Symbols.ELLIPSES))));
  }


  @Test
  public void listFromArgs() {
    eval(" f<- function(...) list(...) ");
    
    assertThat( eval("f(1,2,3)"), elementsIdenticalTo(list(1d, 2d, 3d)));
  }

  @Test
  public void returnInPromises() {

    eval(" f <- function() { " +
              "g <- function(expr) expr ; " +
              "g(return(42)) ; " +
              "return(-1) " +
        "}");


    assertThat( eval("f()"), elementsIdenticalTo(c(42)));
  }
  

  @Test
  public void quoteSymbol() {
    eval("x <- quote(y)");
    assertThat( eval("x"), identicalTo(symbol("y")));
  }

  @Test
  public void symbolToCharacter() {
    assertThat( eval(" as.character(quote(x)) "), elementsIdenticalTo( c("x")));
  }

  @Test
  public void doSwitch() {
    
    assertThat( eval("switch('z', alligator=4,aardvark=2, 44)"), elementsIdenticalTo( c(44)));
    assertThat( eval("switch('a', alligator=4,aardvark=2, 44)"), elementsIdenticalTo( c(44)));
    assertThat(eval("switch('a', alligator=4,aardvark=2)"), identicalTo(NULL));
    assertThat(eval("switch('all', alligator=4,aardvark=2)"), elementsIdenticalTo(c(4)));
    assertThat(eval("switch('all')"), identicalTo(NULL));

    assertThat(eval("switch(1, 'first', 'second')"), elementsIdenticalTo(c("first")));
    assertThat(eval("switch(2, 'first', 'second')"), elementsIdenticalTo(c("second")));
    assertThat( eval("switch(99, 'first', 'second')"), identicalTo( NULL ));
    assertThat( eval("switch(4)"), identicalTo( NULL ));

    assertThat(eval("switch('a', a=,b=,c=3) "), elementsIdenticalTo(c(3)));
    assertThat( eval("switch(NA_character_, a=1,b=2)"), identicalTo( NULL ));
  }

  @Test
  public void useMethod() {
    eval("fry <- function(what, howlong) UseMethod('fry') ");
    eval("fry.default <- function(what, howlong) list(desc='fried stuff',what=what,howlong=howlong) ");
    eval("fry.numeric <- function(what, howlong) list(desc='fried numbers',number=what,howlong=howlong)");

    eval("x<-33");
    eval("class(x) <- 'foo'");

    assertThat( eval("fry(1,5)"), identicalTo( eval("structure(list(desc = \"fried numbers\", number = 1, howlong = 5), .Names = c(\"desc\",  \"number\", \"howlong\"))") ) );
    assertThat(eval("fry(x,15)"), identicalTo(eval("structure(list(desc = \"fried stuff\", what = structure(33, class = \"foo\"),      howlong = 15), .Names = c(\"desc\", \"what\", \"howlong\"))")));

    eval("cook <- function() { eggs<-6; fry(eggs, 5) }");

    assertThat(eval("cook()"), elementsIdenticalTo(eval("list(desc='fried numbers', what=6, howlong=5) ")));
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
   
    assertThat(eval("f(m)"), elementsIdenticalTo(c("matrix")));
  }
  
  @Test
  public void useMethodDispatchesToDoubleThenNumeric() {
    eval("f <- function(x) UseMethod('f')");
    eval("f.numeric <- function(x) 'numeric' ");
    eval("f.double <- function(x) 'double' ");
    
   
    assertThat(eval("f(9)"), elementsIdenticalTo(c("double")));
    eval("f.integer <- function(x) 'integer'");

    assertThat(eval("f(9)"), elementsIdenticalTo(c("double")));
    assertThat(eval("f(9L)"), elementsIdenticalTo(c("integer")));

    assertThat(eval("class(9)"), elementsIdenticalTo(c("numeric")));
    assertThat(eval("class(9L)"), elementsIdenticalTo(c("integer")));
  }

  @Test
  public void nargs() {
    eval("test <- function(a, b = 3, ...) {nargs()}");

    assertThat(eval("test()"), elementsIdenticalTo(c_i(0)));
    assertThat(eval("test(clicketyclack)"), elementsIdenticalTo(c_i(1)));
    assertThat( eval("test(c1, a2, rr3)"), elementsIdenticalTo( c_i(3)));
  }

  @Test
  public void delayedAssign() {

    eval(" delayedAssign('x', f(y)) ");
    eval(" y<-3");
    eval(" f<-function(x) x^2 ");

    assertThat( eval("x"), elementsIdenticalTo(c(9)));
  }

  @Test
  public void evalWithPairList() {
    eval(" params <- list(a=1,b=99)");
    eval(" c<-25");
    assertThat( eval( ".Internal(eval(quote((a+b)/c), params, globalenv()))") , elementsIdenticalTo(c(4)));
  }
  
  @Test
  public void rhsIsEvaledOnlyOnce() {
    eval(" onlyonce <- function() { " +
          		"if(!is.null(globalenv()$once)) stop(); " +
          		" .Internal(assign('once', 1, globalenv(), FALSE));" +
          		" 16 }");
    eval(" k <- list(1,2,3) ");
    eval(" k[[2]] <- onlyonce()");

    assertThat(eval("k"), elementsIdenticalTo(list(1d, 16d, 3d)));

  }

  @Test
  public void intermediateAssignmentTargetsAreNotEvaled() {
    eval(" x<- quote(shouldNotBeEvaled()) ");
    eval(" attr(x, 'foo') <- 'bar' ");
    eval(" environment(x) <- globalenv() ");
    eval(" class(x) <- 'foo' ");
  }
  
  @Test
  public void matchCall() throws IOException {

    
    eval("f<-function(a,b) match.call()");
    eval("matched <- f(b=1,a=2)");
    
    assertThat(eval("matched$a"), elementsIdenticalTo(c(2)));
    assertThat(eval("matched$b"), elementsIdenticalTo(c(1)));
  }
  

  @Test
  public void matchCallWithMissingArgs() throws IOException {

    eval("f<-function(a,b) match.call()");
    eval("matched <- f(b=1)");
    
    assertThat(eval("length(matched)"), elementsIdenticalTo(c_i(2)));
  }
  
  @Test
  public void noPartialMatchingOnArgumentsFollowingElipses() {
    eval("f<-function(..., aardvark) names(list(...))");
    assertThat(eval("f(a=1)"), elementsIdenticalTo(c("a")));
  }
  
  @Test
  public void partialMatchingOnArgumentsPrecedingElipses() {
    eval("f<-function(aardvark=0, ... , aard) aardvark");
    //assertThat(eval("f(aard=1)"), equalTo(c(0))); // match exactly to "aard"
    assertThat(eval("f(aar=1)"), elementsIdenticalTo(c(1))); // match partially to arguments preceding elipses
  }
  
  @Test
  public void matchCallDotsNotExpanded() throws IOException {

    
    eval("f<-function(expand.dots,...) match.call(expand.dots=expand.dots)");
    
    // try without dots expanded
    eval("matched <- f(expand.dots=FALSE, 1,2,3)");
    
    assertThat(eval("as.list(matched$...)"), elementsIdenticalTo(list(1d,2d,3d)));
    
    // now with dots expanded
    eval("matched <- f(expand.dots=TRUE, 44, 55, 90, 50)");
    
    assertThat(eval("matched$..."), identicalTo(NULL));
    assertThat(eval("length(matched)"), elementsIdenticalTo(c_i(6)));
  }

  @Test
  public void primitive() {
    eval("f <- .Primitive('if')");
    assertThat(global.getVariable(topLevelContext, "f"), instanceOf(IfFunction.class));
  } 
  

  @Test
  public void lapplyWithFunctionCalls() throws IOException {
    eval("g<-function(x) .Internal(as.vector(x, 'list'))");
    eval("f<-function(x) g(substitute(x))");
    eval("z<-f(~(0+births))");
      

    assertThat(eval(".Internal(typeof(z[[2]]))"), elementsIdenticalTo(c("language")));
  }

  @Test
  public void nextMethodWithMissing() {
    eval("NextMethod <- function (generic = NULL, object = NULL, ...) " + 
        ".Internal(NextMethod(generic, object, ...))");
    eval("`[.foo` <- function(x, ..., drop = explode()) NextMethod() ");
    eval("x<-1");
    eval("class(x) <- 'foo'");
    eval("x[1]");
  }

  @Test
  public void nextMethodClosure() {
    eval("NextMethod <- function (generic = NULL, object = NULL, ...) " +
        ".Internal(NextMethod(generic, object, ...))");
    eval("g.default <- function(x, b = 42) b ");
    eval("g.foo <- function(x, b = 22) NextMethod() ");
    eval("g <- function(x,b = 16) UseMethod('g') ");
    eval("x<-1");
    eval("class(x) <- 'foo'");
    assertThat(eval("g(x)"), elementsIdenticalTo(c(42)));
  }

  @Test
  public void nextMethodArgReorder() {
    eval("NextMethod <- function (generic = NULL, object = NULL, ...) " +
        ".Internal(NextMethod(generic, object, ...))");
    eval("g.default <- function(b,a) b ");
    eval("g.foo <- function(a,b) NextMethod() ");
    eval("g <- function(a,b) UseMethod('g') ");
    eval("x<-1");
    eval("class(x) <- 'foo'");
    assertThat(eval("g(41,42)"), elementsIdenticalTo(c(41)));
  }

  
  @Test
  public void nextMethodWithMissingFirstArg() {
    eval("NextMethod <- function (generic = NULL, object = NULL, ...) " +
        ".Internal(NextMethod(generic, object, ...))");
    eval("g.default <- function(x = 42) x ");
    eval("g.foo <- function(x = explode()) NextMethod() ");
    eval("g <- function(x) UseMethod('g') ");
    eval("x<-1");
    eval("class(x) <- 'foo'");
    assertThat(eval("g()"), elementsIdenticalTo(c(42)));
  }
  
  @Test
  public void nextMethodWithMissingArg() {
    eval("NextMethod <- function (generic = NULL, object = NULL, ...) " +
        ".Internal(NextMethod(generic, object, ...))");

    eval("g.default <- function(x,...) nargs() ");
    eval("g.foo <- function(x,i,j) NextMethod() ");
    eval("g <- function(x,i,j) UseMethod('g') ");
    eval("x<-1");
    eval("class(x) <- 'foo'");
    assertThat(eval("g(x,1)"), elementsIdenticalTo(c_i(2)));
    assertThat(eval("g(x,1,2)"), elementsIdenticalTo(c_i(3)));
  }
  
  @Test
  public void subsetWithinUseMethod() {
    eval("f.foo <- function(x, filter) { e <- substitute(filter); l <- list(a=42,b=3); " + 
        ".Internal(eval(e, l, NULL)); }");
    eval("f <- function(x, filter) UseMethod('f') ");
    eval("x <- 1");
    eval("class(x) <- 'foo'");
    assertThat(eval("f(x, a+b)"), elementsIdenticalTo(c(45)));
  }
  
  @Test
  public void funCallInClosure() {
    eval("fn <- function(x) x ");
    eval("f <- function(fn) fn(16) ");
    eval("g <- function(fn) f(fn) ");
    eval("h <- sqrt");
    assertThat(eval("g(h)"), elementsIdenticalTo(c(4)));
  }
  
 
  @Test(expected=EvalException.class)
  public void missingArgMasksFunction() {
    eval("f <- function(c) c() ");
    eval("f()");
  }


  @Test
  public void correctEnclosingEnvironment() {
    eval("new.env <- function (hash = TRUE, parent = parent.frame(), size = 29L) .Internal(new.env(hash, parent, size))");
    eval("eval <- function(expr, envir = parent.frame()," +
        " enclos = if(is.list(envir) || is.pairlist(envir)) parent.frame() else baseenv()) .Internal(eval(expr, envir, enclos))");
    eval( "parent.frame <- function(n = 1) .Internal(parent.frame(n)) ");
    eval("eval.parent <- function(expr, n = 1){  p <- parent.frame(n + 1); eval(expr , p) } ");
    eval("local <- function (expr, envir = new.env()) eval.parent(substitute(eval(quote(expr), envir)))");
    
    eval("f<-function() { zz <- 42; local({ zz }) }");
    
    assertThat(eval("f()"), elementsIdenticalTo(c(42)));

  }

  @Test
  public void doCallCall() {
    eval("x <- call('function.that.does.not.exist', 'foo')");
  }
  
  @Test
  public void doCallWithNoArguments() {
    FunctionCall call = (FunctionCall) eval("call('my.function') ");
    
    assertTrue(call.getArguments().length() == 0);
    assertThat(call.getFunction(), identicalTo(Symbol.get("my.function")));
  }
  
  @Test
  public void evalWithNumericEnv() {
    eval(" f <- function() eval(quote(x), envir=0L) ");
    eval(" environment(f) <- new.env() ");
    eval(" x <- 42" );
    assertThat(eval("f()"), elementsIdenticalTo(c(42)));
  }
  
  @Test
  public void evalWithNumericNegEnv() {
    eval(" f <- function() eval(quote(x), envir=-2L) ");
    eval(" g <- function() { x<- 43; f() }");
    assertThat(eval("g()"), elementsIdenticalTo(c(43)));
  }

  @Test
  public void varArgsRef() {
    eval(" f <- function(...) ..1 ");
    eval(" g <- function(...) missing(..1) ");

    assertThat(eval("f(41,42,43)"), elementsIdenticalTo(c(41)));
    assertThat(eval("g(1)"), elementsIdenticalTo(c(false)));
    assertThat(eval("g()"), elementsIdenticalTo(c(true)));
  }
  
  @Test
  public void unboundEnvironmentSubsetting() {
    eval("e <- new.env()");
    eval("x <- e[['noSuchSymbol']]");
    
    assertThat(eval("x"), instanceOf(Null.class));
  }

  @Test
  public void warningFromTopLevel() {
    eval("warning('too much caffeine.')");
  }
  
  @Test
  public void catchErrors() {
    eval("x <- tryCatch( stop('foo') , error = function(e) e)");
    
    assertThat(eval("class(x)"), elementsIdenticalTo(c("simpleError", "error", "condition")));
  }

  @Test
  public void catchErrorsAndHandle() {
    eval("x <- tryCatch( stop('foo') , error = function(e) 42)");

    assertThat(eval("x"), elementsIdenticalTo(c(42)));
  }
  
  @Test
  public void signalErrorUnhandled() {
    eval("x <- tryCatch( { signalCondition(simpleError('STOP')); 46 } )");
    
    assertThat(eval("x"), elementsIdenticalTo(c(46)));
  }

  @Test
  public void signalErrorHandled() {
    eval("x <- tryCatch( { signalCondition(simpleError('STOP')); 46 }, error = function(e) 42 )");

    assertThat(eval("x"), elementsIdenticalTo(c(42)));
  }
  
  @Test
  public void catchEvalExceptions() {
    eval(" p <- quote(foo(x)) ");
    eval(" x <- tryCatch({ p[NA] <- list }, error = function(e) 42) ");
    
    assertThat(eval("x"), elementsIdenticalTo(c(42)));
  }
  
  @Test
  public void caughtWarnings() {
    eval("x <- tryCatch({ warning('foo'); 'not caught' }, warning = function(e) e)");
    
    assertThat(eval("class(x)"), elementsIdenticalTo(c("simpleWarning", "warning", "condition")));
    assertThat(eval("x$message"), elementsIdenticalTo(c("foo")));
    assertThat(eval("x$call[[1]]"), identicalTo(symbol("doTryCatch")));
  }
  
  @Test
  public void namedElipses() {
    eval("g <- function(...) list(...) ");
    eval("f <- function(...) g(...)");
    
    eval(" x <- f(...=1, 2) ");
    
    assertThat(eval("x"), elementsIdenticalTo(list(1d, 2d)));
    assertThat(eval("names(x)"), elementsIdenticalTo(c("...", "")));
  }

  @Test
  public void elipsesAsArg() {
    eval("g <- function(...) list(...) ");
    eval("f <- function(...) g(...)");
    
    eval("f(a=1,2)");
  }
  
  
  @Test
  public void namedEllipsesToBuiltin() {
    ListVector x = (ListVector) eval("list(... = 1, b = 2, 3) ");
    
    assertThat(x.length(), equalTo(3));
    assertThat(x.getNames().getElementAsString(0), equalTo("..."));
    assertThat(x.getNames().getElementAsString(1), equalTo("b"));

  }
  
  @Test
  public void repromisedNotMissing() {
    
    eval("`f<-` <- function(lhs, value) missing(value)");
    eval("x <- 1");
    eval("y <- 2");
    eval("f(x) <- y"); 
    assertThat(eval("x"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void repromisedMissing() {

    eval("`f<-` <- function(lhs, value) missing(value)");
    eval("g <- function(y=1) { x<-1; f(x) <- y; x; }");
    assertThat(eval("g()"), elementsIdenticalTo(c(false)));
  }
  
  @Test
  public void missingEvaluatedPromise() {
    
    eval("g <- function(y=1) { y+1; missing(y); }");
    assertThat(eval("g()"), elementsIdenticalTo(c(true)));
  }
  
  @Test
  public void missingGroupDispatch() {
    eval("`+.foo` <- function(x, y) { missing(y) }");
    eval("f <- function(a) { a+a } ");
    eval("x <- 1");
    eval("class(x) <- 'foo'");
    assertThat(eval("f(x)"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void missingnessDoesNotPropogate() {
    eval("g <- function(y = NULL) missing(y)");
    eval("f <- function(x = NULL) g(y = x)");
    assertThat(eval("f()"), elementsIdenticalTo(c(false)));
  }
}

