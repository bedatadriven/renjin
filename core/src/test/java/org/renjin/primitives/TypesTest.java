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

import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.renjin.sexp.Logical.FALSE;
import static org.renjin.sexp.Logical.TRUE;


public strictfp class TypesTest extends EvalTestCase {

  @Test
  public void asCharacter() {
    assertThat( eval("as.character(1)"), equalTo( c("1") ));
    assertThat( eval("as.character(\"foobar\")"), equalTo( c("foobar") ));
    assertThat( eval("as.character(1L)"), equalTo( c("1") ));
    assertThat( eval("as.character(1.3333333333333333333333333333333333)"),
        equalTo(c("1.33333333333333")));
    assertThat( eval("as.character(TRUE)"), equalTo( c("TRUE") ));
    assertThat( eval("as.character(1000)"), equalTo( c("1000") ));
  }
  
  @Test
  public void asCharacterWithNA() {
    assertThat( eval("as.character(NA)"), equalTo( c( StringVector.NA )) );
  }

  @Test
  public void asCharacterFromStringObject(){ 
    eval("import(java.lang.String)");
    eval("x<-String$new(\"foo\")");
    assertThat(eval("as.character(x)"),equalTo(c("foo")));
  }
  
  @Test
  public void asCharacterFromList() {
    assertThat( eval("as.character(list(3, 'a', TRUE)) "), equalTo( c("3", "a", "TRUE" )));
    assertThat( eval("as.character(list(c(1,3), 'a', TRUE)) "), equalTo( c("c(1, 3)", "a", "TRUE" )));
  }

  @Test
  public void asCharacterFromSymbol() {
    assertThat( eval(" as.character(quote(x)) "), equalTo( c("x") ));
  }

  @Test
  public void asCharacterFromNull() {
    eval( " x<- NULL");
    eval( " g<-function(b) b");
    eval( " f<-function(a) g(as.character(a)) ");
    assertThat( eval("f(x)"), equalTo((SEXP)new StringArrayVector()));
  }

  
  @Test
  public void asDoubleFromDoubleObject(){ 
    eval("import(java.lang.Double)");
    eval("x<-Double$new(1.5)");
    assertThat(eval("as.double(x)"),equalTo(c(1.5)));
  }
  
  @Test
  public void asDoubleFromDouble() {
    assertThat( eval("as.double(3.14)"), equalTo( c(3.14) ) );
    assertThat( eval("as.double(NA_real_)"), equalTo( c(DoubleVector.NA) ) );
  }

  @Test
  public void asDoubleFromInt() {
    assertThat( eval("as.double(3L)"), equalTo( c(3l) ));
  }

  @Test
  public void asLogicalFromBooleanObject(){ 
    eval("import(java.lang.Boolean)");
    eval("x<-Boolean$new(TRUE)");
    assertThat(eval("as.logical(x)"),equalTo(c(TRUE)));
  }
  
  @Test
  public void asLogicalFromList() {
    assertThat( eval("as.logical(list(1, 99.4, 0, 0L, FALSE, 'TRUE', 'FOO', 'T', 'F', 'FALSE')) "),
        equalTo( c(TRUE, TRUE, FALSE, FALSE, FALSE, TRUE, Logical.NA, TRUE, FALSE, FALSE) ));
  }

  @Test
  public void asLogical() {
    assertThat( eval("as.logical(c(1, 99.4, 0, NA_real_)) "),
        equalTo( c(TRUE, TRUE, FALSE, Logical.NA) ));
  }

  @Test
  public void asDoubleFromLogical() {
    assertThat( eval("as.double(TRUE)"), equalTo( c(1d) ));
    assertThat( eval("as.double(FALSE)"), equalTo( c(0d) ));
  }

  @Test
  public void asDoubleFromString() {
    assertThat( eval("as.double(\"42\")"), equalTo( c(42d) ));
    assertThat( eval("as.double(\"not an integer\")"), equalTo( c(DoubleVector.NA) ));
  }

  @Test
  public void asIntFromIntegerObject(){ 
    eval("import(java.lang.Integer)");
    eval("x<-Integer$new(2)");
    assertThat(eval("as.integer(x)"),equalTo(c_i(2)));
  }
  
  @Test
  public void asIntFromDouble() {
    assertThat( eval("as.integer(3.1)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(3.9)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(NA_real_)"), equalTo( c_i( IntVector.NA )));
  }

  @Test
  public void asIntFromRecycledDouble() {
    assertThat( eval("as.integer(c(1, 9.32, 9.9, 5.0))"), equalTo( c_i(1, 9, 9, 5 )));
  }

  @Test
  public void attributesSetting() {
    eval( " v <- .Internal(Version())");
    eval( " attributes(v) <- c( class='simpleList', attributes(v)) ");

    assertThat( eval("v$minor"), not(CoreMatchers.equalTo(NULL)));
  }
  
  @Test
  public void attributeReplace() {
    eval( "x <- 1:12");
    eval( "dim(x) <- c(3,4)");
    eval( "dim(x) <- c(3,4,1)");
    
    assertThat( eval("dim(x)"), equalTo(c_i(3,4,1)));
  
    eval( "dim(x) <- NULL ");
    assertThat( eval("dim(x)"), equalTo(NULL));
  }

  @Test(expected=EvalException.class)
  public void attributeReplaceOnNull() {
    eval( "dim(NULL) <- c(0,4)");
  }

    

  @Test
  public void na() {
    assertThat( eval(" is.na(TRUE) "), equalTo( c(FALSE)));
    assertThat( eval(" is.na(NA) "), equalTo( c(TRUE)));
    assertThat( eval(" is.na(c(1L, NA_integer_)) "), equalTo( c(FALSE, TRUE)));
    assertThat( eval(" is.na(c(NA_character_, '', 'foo')) "), equalTo( c(TRUE, FALSE, FALSE)));
    assertThat( eval(" is.na(c()) "), equalTo((SEXP)LogicalVector.EMPTY));
  }


  @Test
  public void finite() {
    assertThat( eval("is.finite(42)"), equalTo(c(true)));
    assertThat( eval("is.finite(1/0)"), equalTo(c(false)));
    assertThat( eval("is.finite(1/0)"), equalTo(c(false)));
    assertThat( eval("is.finite(NA)"), equalTo(c(false)));
    assertThat( eval("is.finite(NaN)"), equalTo(c(false)));
  }

  @Test
  public void infinite() {
    assertThat( eval("is.infinite(1)"), equalTo(c(false)));
    assertThat( eval("is.infinite(1/0)"), equalTo(c(true)));
    assertThat( eval("is.infinite(NA)"), equalTo(c(false)));
    assertThat( eval("is.infinite(NaN)"), equalTo(c(false)));
  }

  @Test
  public void isnan() {
    assertThat( eval("is.nan(1)"), equalTo(c(false)));
    assertThat( eval("is.nan(1/0)"), equalTo(c(false)));
    assertThat( eval("is.nan(NA)"), equalTo(c(false)));
    assertThat( eval("is.nan(sqrt(-2))"), equalTo(c(true)));
  }

  @Test
  public void naList() {
    assertThat( eval(" is.na(list(NULL,  1,     FALSE, c(NA,4), NA_integer_, NA_real_)) "),
                       equalTo( c(FALSE, FALSE, FALSE, FALSE,   TRUE,        TRUE)) );
  }

  @Test
  public void naPreservesNames() {
    assertThat( eval(" names(is.na(c(x=1,y=2))) "), equalTo( c("x", "y")));
  }
  
//  @Test
//  public void unaryOpPreservesAllAttributes() {
//    eval("x <- 1:9");
//    eval("attr(x,'foo') <- 'bar' ");
//    assertThat( eval("attr(x, 'foo')"), equalTo()
//  }


  @Test
  public void naPreservesDimNames() {
    eval( " x <- .Internal(rbind(1, c(a=1,b=2))) ");
    eval( " x <- is.na(x) ");
    assertThat( eval(" dimnames(x)[[2]] "), equalTo( c("a", "b")));

    eval(" x <- !x ");
    assertThat( eval(" dimnames(x)[[2]] "), equalTo( c("a", "b")));
  }
  
  @Test
  public void nullDimNamePreservedOnAssignment() {
    eval(" x <- 1:12");
    eval(" dim(x) <- c(3,4) ");
    eval(" dimnames(x) <- list(NULL, c('a','b','c','d'))");
    
    assertThat(eval("dimnames(x)[[1]]"), equalTo(NULL));
  }


  @Test
  public void unaryPreservesNames() {
    assertThat( eval(" names(!is.na(c(x=1,y=2)))"), equalTo( c("x", "y")));
  }

  @Test
  public void vector() {
    assertThat( eval(" .Internal(vector('list', 3)) "), equalTo( list(NULL, NULL, NULL)));
    assertThat( eval(" .Internal(vector('numeric', 2)) "), equalTo( c(0, 0)));
    assertThat( eval(" .Internal(vector('character', 3)) "), equalTo( c("","","")) );
    assertThat( eval(" .Internal(vector('logical', 2)) "), equalTo( c(FALSE, FALSE)) );
  }

  @Test
  public void environment() {
    eval(" environment <- function(fun=NULL) .Internal(environment(fun)) ");
    eval(" f <- function() { qqq<-42; environment()$qqq }");
    assertThat( eval("f()"), equalTo(c(42)));

  }
  
  @Test
  public void environmentCalledFromPromise() {
    eval(" environment <- function(fun=NULL) .Internal(environment(fun)) ");
    eval(" g <- function(env) env$zz ");
    eval(" h <- function() { zz<-33; g(environment()); }");
    assertThat( eval("h()"), equalTo(c(33)));
  }
  
  @Test
  public void env2list() {
    eval(" env <- .Internal(new.env(TRUE, globalenv(), 29L))");
    eval(" env$a <- 1");
    eval(" env$.a <- 2");
    eval(" x <- .Internal(env2list(env,FALSE))");
    eval(" y <- .Internal(env2list(env,TRUE))");

    assertThat( eval("names(x)"), CoreMatchers.equalTo(c("a")));
    assertThat( eval("names(y)"), CoreMatchers.equalTo(c("a",".a")));
  }
  
  @Test
  public void env2list_hiddenFirst() {
    eval(" env <- .Internal(new.env(TRUE, globalenv(), 29L))");
    eval(" env$.a <- 1");
    eval(" env$a <- 2");
    eval(" x <- .Internal(env2list(env,FALSE))");
    eval(" y <- .Internal(env2list(env,TRUE))");

    assertThat( eval("names(x)"), CoreMatchers.equalTo(c("a")));
    assertThat( eval("names(y)"), CoreMatchers.equalTo(c("a",".a")));
  }
  
  @Test
  public void env2list_multipleNonHidden() {
    eval(" env <- .Internal(new.env(TRUE, globalenv(), 29L))");
    eval(" env$a <- 1");
    eval(" env$b <- 2");
    eval(" x <- .Internal(env2list(env,FALSE))");
    eval(" y <- .Internal(env2list(env,TRUE))");

    assertThat( eval("names(x)"), CoreMatchers.equalTo(c("a","b")));
    assertThat( eval("names(y)"), CoreMatchers.equalTo(c("a","b")));
  }
  
  @Test
  public void environmentName() {
    assertThat( eval(".Internal(environmentName(baseenv()))"), CoreMatchers.equalTo(c("base")));
    assertThat( eval(".Internal(environmentName(globalenv()))"), CoreMatchers.equalTo(c("R_GlobalEnv")));
  }

  @Test
  public void environmentOfRandomExp() {
    assertThat( eval(".Internal(environment(1))"), is((SEXP) Null.INSTANCE));
  }

  @Test
  public void environmentOfClosure() {
    eval("f <- function() { 1 } ");
    assertThat( eval(".Internal(environment( f ))"), is((SEXP) topLevelContext.getGlobalEnvironment()));
  }

  @Test
  public void list() {
    assertThat( eval("list(\"a\")"), equalTo( list("a") ));
  }

  @Test
  public void listOfNulls() {
    assertThat( eval("list(NULL, NULL)"), equalTo( list(NULL, NULL) ));
  }

  @Test
  public void listOfNull() {
    assertThat( eval("list(NULL)"), equalTo( list(NULL) ));
  }
  
  @Test
  public void closureBody() {
    eval(" f <- function(x) sqrt(x) ");
    
    assertThat( eval(" .Internal(body(f))[[1]] "), CoreMatchers.equalTo(symbol("sqrt")));
  }

  @Test
  public void setClassWithAttrFunction() {
    eval(" x<-c(1,2,3) ");
    eval(" attr(x, 'class') <- 'foo' ");

    assertThat( eval(" class(x) "), equalTo( c("foo")));
  }
  
  @Test
  public void asFunctionDefault() {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    list.add("a", Symbol.MISSING_ARG);
    list.add("b", new DoubleArrayVector(2));
    list.add(FunctionCall.newCall(Symbol.get("+"), Symbol.get("a"), Symbol.get("b")));
    global.setVariable(Symbol.get("x"), list.build());
    
    eval("f <- .Internal(as.function.default(x, globalenv()))");
    assertThat(eval("f(1)"), equalTo(c(3)));
    assertThat(eval("f(1,3)"), equalTo(c(4)));
  }

  @Test
  public void dimAttributesAreConverted() {
    eval(" x <- 1");
    eval(" attributes(x) <- list(dim=1)");
  }

  @Test
  public void atomicVectorsHaveImplicitClasses() {
    assertThat( eval("class(9)"), equalTo(c("numeric")));
    assertThat( eval("class(9L)"), equalTo(c("integer")));
    assertThat( eval("class('foo')"), equalTo(c("character")));
    assertThat( eval("class(TRUE)"), equalTo(c("logical")));
    assertThat( eval("class(NULL)"), equalTo(c("NULL")));
  }
  
  @Test
  @Ignore("to implement")
  public void someSpecialFunctionsHaveTheirOwnImplicitClass() {
    assertThat( eval("class(quote({1}))"), equalTo(c("{")));
    assertThat( eval("class(quote(if(TRUE) 1 else 0))"), equalTo(c("if")));
    assertThat( eval("class(quote(while(TRUE) 1))"), equalTo(c("while")));
    assertThat( eval("class(quote(for(x in 1:9) x))"), equalTo(c("for")));
 //   assertThat( eval("class(quote(x=1)"), equalTo(c("=")));
    assertThat( eval("class(quote(x<-1)"), equalTo(c("<-")));
    assertThat( eval("class(quote((1+1))"), equalTo(c("(")));
  }
  
  @Test
  public void implicitClassesAreOverridenByClassAttribute() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    eval("class(m) <- c('foo','bar')");
    assertThat( eval("class(m)"), equalTo(c("foo", "bar")));        
  }

  @Test
  public void matricesHaveImplicitClass() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    assertThat( eval("class(m)"), equalTo(c("matrix")));    
  }
  
  @Test
  public void matricesAreNotObjects() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    assertThat( eval("is.object(m)"), equalTo(c(false)));
  }
  
  @Test
  public void arraysHaveImplicitClass() {
    eval("a <- 1:12");
    eval("dim(a) <- 12");
    assertThat( eval("class(a)"), equalTo(c("array")));
  }
  
  
  
  @Test
  public void unclass() {
    eval("x<-1");
    eval("class(x) <- 'foo'");
    eval("x <- unclass(x)");
    assertThat(eval("class(x)"), equalTo(c("numeric")));
  }
  
  @Test
  public void unclassPreservesOtherAttribs() {
    eval("x<-1");
    eval("attr(x,'zing')<-'bat'");
    eval("class(x) <- 'foo'");
    eval("x <- unclass(x)");
    assertThat(eval("class(x)"), equalTo(c("numeric")));
    assertThat(eval("attr(x,'zing')"), equalTo(c("bat")));

  }

  @Test
  public void setNamesWithNonStrVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- c(4,5,6) ");

    assertThat( eval("names(x)"), equalTo( c("4", "5","6")));
  }

  @Test
  public void setNamesWithNonVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- quote(quote(z)) ");

    assertThat( eval("names(x)"), equalTo( c("z", StringVector.NA, StringVector.NA)));
  }

  @Test
  public void setAttributes() {
    eval(" x <- 1:5");
    eval(" attributes(x) <- list(names=c('a','b', 'c'), foo='bar') ");

    assertThat( eval(" names(x) "), equalTo(c("a","b","c",StringVector.NA,StringVector.NA)));
    assertThat( eval(" attr(x, 'foo') "), equalTo( c("bar")));

  }

  @Test
  public void asEnvironment() {
    assertThat( eval("as.environment(1)"), sameInstance((SEXP)topLevelContext.getGlobalEnvironment()));
    assertThat( eval("as.environment(2)"), sameInstance((SEXP)topLevelContext.getGlobalEnvironment().getParent()));
  }

  @Test
  public void asVector() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");

    assertThat( eval("as.vector(1, 'character')"), equalTo( c("1" )));
    assertThat( eval("as.vector(c(4,5,0), mode='logical')"), equalTo( c(true, true, false)));
    assertThat( eval("as.vector(c(TRUE,FALSE,NA), mode='double')"), equalTo( c(1.0,0,DoubleVector.NA)));
}


  @Test
  public void asPairList() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");
    eval(" x <- as.vector( c(a=1,b=2), mode = 'pairlist') ");

    PairList.Node head = (PairList.Node) global.getVariable("x");
    assertThat( head.length(), equalTo(2));
    assertThat( head.getNode(0).getTag(), equalTo( symbol("a")));
    assertThat( head.getElementAsSEXP(0), equalTo( c(1) ));
    assertThat(head.getNode(1).getTag(), equalTo( symbol("b") ));
    assertThat( head.getElementAsSEXP(1), equalTo( c(2) ));
  }

  @Test
  public void options() {
    eval(" .Internal(options(foo=TRUE)) ");
  }

  @Test
  public void pairListToList() {

    eval(" x <- .Internal(as.vector(list(a=41, b=42), 'pairlist')) ");
    eval(" y <- .Internal(as.vector(x, 'list')) ");

    assertThat( eval("y"), equalTo( list(41d,42d)));
    assertThat( eval("names(x)"), equalTo( c("a", "b")));
    assertThat( eval(".Internal(typeof(x))"), equalTo( c("pairlist")));
    assertThat( eval("names(y)"), equalTo( c("a", "b")));
  }

  @Test
  public void functionCallToList() {

    eval(" x <- quote(~(0+births)) ");
    eval(" y <- .Internal(as.vector(x, 'list')) ");

    assertThat( eval("length(y)"), equalTo( c_i(2)));
    assertThat( eval("names(y)"), equalTo(  NULL ));
    assertThat( eval(".Internal(typeof(y[[2]]))"), equalTo( c("language")));
  }
  
  @Test
  public void setLength(){
    eval("x <- c(1,2,3)");
    assertThat(eval("length(x)"), equalTo(c_i(3)));
    eval("length(x)<-4");
    assertThat(eval("length(x)"), equalTo(c_i(4)));
    assertThat(eval("is.na(x[4])"),equalTo(c(true)));
    
    eval("length(x) <- 2");
    assertThat(eval("x"), equalTo(c(1,2)));
  }

  @Test
  public void setLengthWithNames() {
    eval("x <- c(a=1,b=2,c=3)");
    eval("attr(x, 'foo') <- 'baz'");

    eval("length(x)<-2");
    assertThat(eval("length(x)"), equalTo(c_i(2)));
    assertThat(eval("length(names(x))"),equalTo(c_i(2)));
    assertThat(eval("attr(x,'foo')"),equalTo(NULL));

  }
  
  @Test
  public void isRawAndAsRaw(){
    Raw r1 = new Raw(1);
    Raw r2 = new Raw(20);
    Raw r3 = new Raw(30);
    assertThat( eval("is.raw(as.raw(c(123,124)))"), equalTo(c(Logical.TRUE)));
    assertThat( eval("as.raw(c(1,20,30))"), equalTo(c(r1,r2,r3)));
  }
  
  @Test
  public void rawToBits(){
    Raw r0 = new Raw(00);
    Raw r1 = new Raw(01);
    assertThat( eval(".Internal(rawToBits(as.raw(c(1,2))))"), equalTo(c(r0,r0,r0,r0,r0,r0,r0,r1,r0,r0,r0,r0,r0,r0,r1,r0)));
  }
  
  @Test
  public void charToRaw(){
    Raw r1 = new Raw('A');
    Raw r2 = new Raw('B');
    Raw r3 = new Raw('C');
    assertThat( eval(".Internal(charToRaw(\"ABC\"))"), equalTo(c(r1,r2,r3)));
  }
  
  @Test
  public void multiByteCharToRaw(){
    Raw r1 = new Raw(0xc2);
    Raw r2 = new Raw(0xa0);
    assertThat( eval(".Internal(charToRaw('\u00a0'))"), equalTo(c(r1,r2)));
  }
  
  @Test
  public void rawShift() {
    Raw r1 = new Raw(0x3a);Raw r2 = new Raw(0x3c);Raw r3 = new Raw(0x3e);
    assertThat(eval(".Internal(rawShift(as.raw(c(29:31)),1))"), equalTo(c(r1, r2, r3)));
    
    //r1 = new Raw(0x0e);r2 = new Raw(0x0f);r3 = new Raw(0x0f);
    //assertThat(eval(".Internal(rawShift(as.raw(c(29:31)),-1))"), equalTo(c(r1, r2, r3)));
  }
  
  @Test
  public void intToBits(){
    RawVector.Builder b = new RawVector.Builder();
    b.add(new Raw(01));
    for (int i=1;i<32;i++) {
      b.add(new Raw(0));
    }
    RawVector rv = b.build();
    assertThat(eval(".Internal(intToBits(1))"), equalTo(c(rv.getAsRawArray())));
  }

  @Test
  public void isNaGeneric() {
    
    eval("x<-1");
    eval("class(x) <- 'foo'");
    
    eval("is.na.foo <- function(x) 'FOO!!'");
    assertThat(eval("is.na(x)"), equalTo(c("FOO!!")));
  }
  
  @Test
  public void rawToChar() {
    
    byte[] bytes = "!\"#$%&'()".getBytes();
    String s = new String(bytes);
    
    assertThat(eval(".Internal(rawToChar(as.raw(32:126), FALSE))"), equalTo(
        c(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~")));
    
  }
  
  @Test
  public void drop() {
    
    eval("x <- 1:12");
    eval("dim(x) <- c(3,1, 4, 1)");
    eval("dimnames(x) <- dimnames(x) <- list(c('r1','r2','r3'), 'd2', c('c1', 'c2', 'c3', 'c4'), 'd4')");
    eval("y <- .Internal(drop(x))");
    
    assertThat(eval("dim(y)"), equalTo(c_i(3,4)));
    assertThat(eval("dimnames(y)[[1]]"), equalTo(c("r1", "r2", "r3")));
    assertThat(eval("dimnames(y)[[2]]"), equalTo(c("c1", "c2", "c3", "c4")));
  }
  
  @Test
  public void identicalS4() {
    topLevelContext.getGlobalEnvironment().setVariable("x", new S4Object());
    topLevelContext.getGlobalEnvironment().setVariable("y", new S4Object());
    eval("attr(x, 'foo') <- 'bar' ");
    eval("attr(y, 'foo') <- 'baz' ");
    
    assertThat(eval(".Internal(identical(x,y,TRUE,TRUE,TRUE,TRUE))"), equalTo(c(false)));

    eval("attr(y, 'foo') <- 'bar' ");

    assertThat(eval(".Internal(identical(x,y,TRUE,TRUE,TRUE,TRUE))"), equalTo(c(true)));
  }
  
  @Test
  public void identical() {
    eval("identical <- function(x,y) .Internal(identical(x,y,TRUE,TRUE,TRUE,TRUE)) ");
    
    assertThat(eval("identical(1,1)"), equalTo(c(true)));
    assertThat(eval("identical(1,1L)"), equalTo(c(false)));
    assertThat(eval("identical(1,NA)"), equalTo(c(false)));
    assertThat(eval("identical(NA,NA)"), equalTo(c(true)));
    assertThat(eval("identical(NA_real_,NA_real_)"), equalTo(c(true)));
    assertThat(eval("identical(1:3,c(1L,2L,3L))"), equalTo(c(true)));
    assertThat(eval("identical(quote(x), quote(y))"), equalTo(c(false)));
    assertThat(eval("identical(quote(x), quote(x))"), equalTo(c(true)));
    assertThat(eval("identical(NULL, NULL)"), equalTo(c(true)));
    assertThat(eval("identical(NULL, 1)"), equalTo(c(false)));
    assertThat(eval("identical(list(x=1,y='foo',NA), list(x=1,y='foo',NA))"), equalTo(c(true)));
    assertThat(eval("identical(function(x) x, function(x) x)"), equalTo(c(false)));
    assertThat(eval("identical(1+3i, 1+4i)"), equalTo(c(false)));
    assertThat(eval("identical(1+3i, 2+3i)"), equalTo(c(false)));
    assertThat(eval("identical(1+3i, 1+3i)"), equalTo(c(true)));
    
    
    
    eval("f<- function(x) x");
    assertThat(eval("identical(f,f)"), equalTo(c(true)));

    eval("y <- x <- 1:12");
    eval("dim(x) <- c(6,2)");
    eval("dim(y) <- c(3,4)");
    assertThat(eval("identical(x,y)"), equalTo(c(false)));
    
    eval("dim(y) <- c(6,2)");
    assertThat(eval("identical(x,y)"), equalTo(c(true)));
    
    eval("attr(x,'foo') <- 'bar'");
    assertThat(eval("identical(x,y)"), equalTo(c(false)));
    
  }
  
  
  @Test
  public void expression() {
    eval(" ex <- expression({ x * 2})");
    eval(" x<-4 ");
    assertThat( eval(".Internal(eval(ex,globalenv(),NULL))"), equalTo(c(8)));
  }

  @Test(expected=EvalException.class) 
  public void getThrowsOnNonExistantVariable() {
    eval(".Internal(get('nonexistant.variable', globalenv(), 'any', TRUE))");
  }
  
  @Test
  public void existsNoInherit() {
    assumingBasePackagesLoad();
    eval("x <- 42");
    eval("f <- function() { exists('x', inherits=FALSE) } ");
    assertThat( eval("f()"), equalTo(c(false)));
  }
  
  @Test
  public void getNoInheritThrows() {
    assumingBasePackagesLoad();
    eval("x <- 42");
    eval("f <- function() { exists('x', inherits=FALSE) } ");
    eval("f()");
  }
  
  @Test
  public void listToEnvironment() {
    eval("x <- as.environment(list(a=42,b=64))");
    assertThat(eval("x$a"), equalTo(c(42)));
    assertThat(eval("x$b"), equalTo(c(64)));

  }
  
  @Test
  public void asEnvironmentWithName() {
    assertThat(eval("as.environment('package:base')"), 
        is((SEXP)topLevelContext.getGlobalEnvironment().getBaseEnvironment()));
  }

  @Test
  public void asGlobalEnv() {
    eval("f <- function() as.environment('.GlobalEnv')");
    eval("environment(f) <- .BaseNamespaceEnv ");
    
    eval("f()");
  }
 
  @Test
  public void convertingDoubleVector() {
    eval("as.numeric(as.character(1:10))");
  }

}
