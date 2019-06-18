/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.renjin.sexp.Logical.FALSE;
import static org.renjin.sexp.Logical.TRUE;


public strictfp class TypesTest extends EvalTestCase {

  @Test
  public void asCharacter() {
    assertThat( eval("as.character(1)"), elementsIdenticalTo( c("1") ));
    assertThat( eval("as.character(\"foobar\")"), elementsIdenticalTo( c("foobar") ));
    assertThat( eval("as.character(1L)"), elementsIdenticalTo( c("1") ));
    assertThat( eval("as.character(1.3333333333333333333333333333333333)"),
        elementsIdenticalTo(c("1.33333333333333")));
    assertThat( eval("as.character(TRUE)"), elementsIdenticalTo( c("TRUE") ));
    assertThat( eval("as.character(1000)"), elementsIdenticalTo(c("1000")));
  }
  
  @Test
  public void coerceWithoutArgument() {
    assertThat( eval("as.character()"), elementsIdenticalTo(c(new String[0])));
    assertThat( eval("as.double()"), identicalTo(DoubleArrayVector.EMPTY));
    assertThat( eval("as.logical()"), identicalTo(LogicalArrayVector.EMPTY));
    assertThat( eval("as.integer()"), identicalTo(IntArrayVector.EMPTY));
    assertThat( eval("as.complex()"), identicalTo(ComplexArrayVector.EMPTY));
  }

  @Test
  public void doubleNaNToComplex() {
    assertThat(eval("is.na(as.complex(NaN))"), elementsIdenticalTo(c(true)));
    assertThat(eval("is.na(as.complex(0/0))"), elementsIdenticalTo(c(true)));
    assertThat(eval("is.na(as.complex(NA))"), elementsIdenticalTo(c(true)));
  }

  @Test
  public void asCharacterWithNA() {
    assertThat( eval("as.character(NA)"), elementsIdenticalTo( c( StringVector.NA )) );
  }

  @Test
  public void asCharacterFromStringObject(){ 
    eval("import(java.lang.String)");
    eval("x<-String$new(\"foo\")");
    assertThat(eval("as.character(x)"), elementsIdenticalTo(c("foo")));
  }
  
  @Test
  public void asCharacterFromList() {
    assertThat( eval("as.character(list(3, 'a', TRUE)) "), elementsIdenticalTo( c("3", "a", "TRUE" )));
    assertThat( eval("as.character(list(c(1,3), 'a', TRUE)) "), elementsIdenticalTo( c("c(1, 3)", "a", "TRUE" )));
  }

  @Test
  public void asCharacterFromSymbol() {
    assertThat( eval(" as.character(quote(x)) "), elementsIdenticalTo( c("x") ));
  }

  @Test
  public void asCharacterFromNull() {
    eval( " x<- NULL");
    eval( " g<-function(b) b");
    eval( " f<-function(a) g(as.character(a)) ");
    assertThat( eval("f(x)"), identicalTo((SEXP)new StringArrayVector()));
  }

  
  @Test
  public void asDoubleFromDoubleObject(){ 
    eval("import(java.lang.Double)");
    eval("x<-Double$new(1.5)");
    assertThat(eval("as.double(x)"), elementsIdenticalTo(c(1.5)));
  }
  
  @Test
  public void asDoubleFromDouble() {
    assertThat( eval("as.double(3.14)"), elementsIdenticalTo( c(3.14) ) );
    assertThat( eval("as.double(NA_real_)"), elementsIdenticalTo( c(DoubleVector.NA) ) );
  }

  @Test
  public void asDoubleFromInt() {
    assertThat( eval("as.double(3L)"), elementsIdenticalTo( c(3l) ));
  }

  @Test
  public void asLogicalFromBooleanObject(){ 
    eval("import(java.lang.Boolean)");
    eval("x<-Boolean$new(TRUE)");
    assertThat(eval("as.logical(x)"), elementsIdenticalTo(c(TRUE)));
  }
  
  @Test
  public void asLogicalFromList() {
    assertThat( eval("as.logical(list(1, 99.4, 0, 0L, FALSE, 'TRUE', 'FOO', 'T', 'F', 'FALSE')) "),
        elementsIdenticalTo( c(TRUE, TRUE, FALSE, FALSE, FALSE, TRUE, Logical.NA, TRUE, FALSE, FALSE) ));
  }

  @Test
  public void asLogical() {
    assertThat( eval("as.logical(c(1, 99.4, 0, NA_real_)) "),
        elementsIdenticalTo( c(TRUE, TRUE, FALSE, Logical.NA) ));
  }

  @Test
  public void asLogicalFromString() {
    assertThat( eval("as.logical('TRUE')"), elementsIdenticalTo(c(true)));
    assertThat( eval("as.logical('FALSE')"), elementsIdenticalTo(c(false)));

    assertThat( eval("as.logical('true')"), elementsIdenticalTo(c(true)));
    assertThat( eval("as.logical('false')"), elementsIdenticalTo(c(false)));

    assertThat( eval("as.logical('T')"), elementsIdenticalTo(c(true)));
    assertThat( eval("as.logical('F')"), elementsIdenticalTo(c(false)));

    assertThat( eval("as.logical('TR')"), elementsIdenticalTo(c(Logical.NA)));
    assertThat( eval("as.logical('FA')"), elementsIdenticalTo(c(Logical.NA)));
  }
  
  @Test
  public void asDoubleFromLogical() {
    assertThat( eval("as.double(TRUE)"), elementsIdenticalTo( c(1d) ));
    assertThat( eval("as.double(FALSE)"), elementsIdenticalTo( c(0d) ));
  }

  @Test
  public void asDoubleFromString() {
    assertThat( eval("as.double(\"42\")"), elementsIdenticalTo( c(42d) ));
    assertThat( eval("as.double(\"not an integer\")"), elementsIdenticalTo( c(DoubleVector.NA) ));
  }

  @Test
  public void asIntFromIntegerObject(){ 
    eval("import(java.lang.Integer)");
    eval("x<-Integer$new(2)");
    assertThat(eval("as.integer(x)"), elementsIdenticalTo(c_i(2)));
  }
  
  @Test
  public void asIntFromDouble() {
    assertThat( eval("as.integer(3.1)"), elementsIdenticalTo( c_i( 3 )));
    assertThat( eval("as.integer(3.9)"), elementsIdenticalTo( c_i( 3 )));
    assertThat( eval("as.integer(NA_real_)"), elementsIdenticalTo( c_i( IntVector.NA )));
  }

  @Test
  public void asIntFromRecycledDouble() {
    assertThat( eval("as.integer(c(1, 9.32, 9.9, 5.0))"), elementsIdenticalTo( c_i(1, 9, 9, 5 )));
  }

  @Test
  public void attributesSetting() {
    eval( " v <- .Internal(Version())");
    eval( " attributes(v) <- c( class='simpleList', attributes(v)) ");

    assertThat( eval("v$minor"), not(equalTo(NULL)));
  }
  
  @Test
  public void attributeReplace() {
    eval( "x <- 1:12");
    eval( "dim(x) <- c(3,4)");
    eval( "dim(x) <- c(3,4,1)");
    
    assertThat( eval("dim(x)"), elementsIdenticalTo(c_i(3,4,1)));
  
    eval( "dim(x) <- NULL ");
    assertThat( eval("dim(x)"), identicalTo(NULL));
  }

  @Test(expected=EvalException.class)
  public void attributeReplaceOnNull() {
    eval( "dim(NULL) <- c(0,4)");
  }

    

  @Test
  public void na() {
    assertThat( eval(" is.na(TRUE) "), elementsIdenticalTo( c(FALSE)));
    assertThat( eval(" is.na(NA) "), elementsIdenticalTo( c(TRUE)));
    assertThat( eval(" is.na(c(1L, NA_integer_)) "), elementsIdenticalTo( c(FALSE, TRUE)));
    assertThat( eval(" is.na(c(NA_character_, '', 'foo')) "), elementsIdenticalTo( c(TRUE, FALSE, FALSE)));
    assertThat( eval(" is.na(c()) "), identicalTo((SEXP)LogicalVector.EMPTY));
  }


  @Test
  public void finite() {
    assertThat( eval("is.finite(42)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.finite(1/0)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.finite(1/0)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.finite(NA)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.finite(NaN)"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void infinite() {
    assertThat( eval("is.infinite(1)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.infinite(1/0)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.infinite(NA)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.infinite(NaN)"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void finiteAtomicVectors() {
    assertThat( eval("is.infinite('Inf')"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.finite('Inf')"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.finite(1L)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.finite(TRUE)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.finite(FALSE)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.infinite(TRUE)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.infinite(FALSE)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.infinite(1L)"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void isnan() {
    assertThat( eval("is.nan(1)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.nan(1/0)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.nan(NA)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.nan(sqrt(-2))"), elementsIdenticalTo(c(true)));
  }

  @Test
  public void isNa() {
    assertThat( eval("is.na(1)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.na(NA)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.na(NA_integer_)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.na(NA_character_)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.na(NA_real_)"), elementsIdenticalTo(c(true)));
    assertThat( eval("is.na(NA_complex_)"), elementsIdenticalTo(c(true)));

    assertThat( eval("is.na(NaN)"), elementsIdenticalTo(c(true)));
    
    assertThat( eval("is.na(1/0)"), elementsIdenticalTo(c(false)));
    assertThat( eval("is.na(0/0)"), elementsIdenticalTo(c(true)));
  }
  
  @Test
  public void naList() {
    assertThat( eval(" is.na(list(NULL,  1,     FALSE, c(NA,4), NA_integer_, NA_real_)) "),
                       elementsIdenticalTo( c(FALSE, FALSE, FALSE, FALSE,   TRUE,        TRUE)) );
  }

  @Test
  public void naPreservesNames() {
    assertThat( eval(" names(is.na(c(x=1,y=2))) "), elementsIdenticalTo( c("x", "y")));
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
    assertThat( eval(" dimnames(x)[[2]] "), elementsIdenticalTo( c("a", "b")));

    eval(" x <- !x ");
    assertThat( eval(" dimnames(x)[[2]] "), elementsIdenticalTo( c("a", "b")));
  }
  
  @Test
  public void nullDimNamePreservedOnAssignment() {
    eval(" x <- 1:12");
    eval(" dim(x) <- c(3,4) ");
    eval(" dimnames(x) <- list(NULL, c('a','b','c','d'))");
    
    assertThat(eval("dimnames(x)[[1]]"), identicalTo(NULL));
  }


  @Test
  public void unaryPreservesNames() {
    assertThat( eval(" names(!is.na(c(x=1,y=2)))"), elementsIdenticalTo( c("x", "y")));
  }

  @Test
  public void vector() {
    assertThat( eval(" .Internal(vector('list', 3)) "), elementsIdenticalTo( list(NULL, NULL, NULL)));
    assertThat( eval(" .Internal(vector('numeric', 2)) "), elementsIdenticalTo( c(0, 0)));
    assertThat( eval(" .Internal(vector('character', 3)) "), elementsIdenticalTo( c("","","")) );
    assertThat( eval(" .Internal(vector('logical', 2)) "), elementsIdenticalTo( c(FALSE, FALSE)) );
  }

  @Test
  public void environment() {
    eval(" environment <- function(fun=NULL) .Internal(environment(fun)) ");
    eval(" f <- function() { qqq<-42; environment()$qqq }");
    assertThat( eval("f()"), elementsIdenticalTo(c(42)));

  }
  
  @Test
  public void environmentCalledFromPromise() {
    eval(" environment <- function(fun=NULL) .Internal(environment(fun)) ");
    eval(" g <- function(env) env$zz ");
    eval(" h <- function() { zz<-33; g(environment()); }");
    assertThat( eval("h()"), elementsIdenticalTo(c(33)));
  }

  
  @Test
  public void environmentName() {
    assertThat( eval(".Internal(environmentName(baseenv()))"), elementsIdenticalTo(c("base")));
    assertThat( eval(".Internal(environmentName(globalenv()))"), elementsIdenticalTo(c("R_GlobalEnv")));
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
    assertThat( eval("list(\"a\")"), elementsIdenticalTo(list("a")));
  }

  @Test
  public void listOfNulls() {
    assertThat( eval("list(NULL, NULL)"), elementsIdenticalTo( list(NULL, NULL) ));
  }

  @Test
  public void listOfNull() {
    assertThat( eval("list(NULL)"), elementsIdenticalTo( list(NULL) ));
  }
  
  @Test
  public void closureBody() {
    eval(" f <- function(x) sqrt(x) ");
    
    assertThat( eval(" .Internal(body(f))[[1]] "), equalTo(symbol("sqrt")));
  }

  @Test
  public void setClassWithAttrFunction() {
    eval(" x<-c(1,2,3) ");
    eval(" attr(x, 'class') <- 'foo' ");

    assertThat( eval(" class(x) "), elementsIdenticalTo(c("foo")));
  }
  
  @Test
  public void asFunctionDefault() {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    list.add("a", Symbol.MISSING_ARG);
    list.add("b", new DoubleArrayVector(2));
    list.add(FunctionCall.newCall(Symbol.get("+"), Symbol.get("a"), Symbol.get("b")));
    global.setVariable(topLevelContext, Symbol.get("x"), list.build());
    
    eval("f <- .Internal(as.function.default(x, globalenv()))");
    assertThat(eval("f(1)"), elementsIdenticalTo(c(3)));
    assertThat(eval("f(1,3)"), elementsIdenticalTo(c(4)));
  }

  @Test
  public void dimAttributesAreConverted() {
    eval(" x <- 1");
    eval(" attributes(x) <- list(dim=1)");
  }

  @Test
  public void atomicVectorsHaveImplicitClasses() {
    assertThat( eval("class(9)"), elementsIdenticalTo(c("numeric")));
    assertThat( eval("class(9L)"), elementsIdenticalTo(c("integer")));
    assertThat( eval("class('foo')"), elementsIdenticalTo(c("character")));
    assertThat(eval("class(TRUE)"), elementsIdenticalTo(c("logical")));
    assertThat(eval("class(NULL)"), elementsIdenticalTo(c("NULL")));
  }
  
  @Test
  @Ignore("to implement")
  public void someSpecialFunctionsHaveTheirOwnImplicitClass() {
    assertThat( eval("class(quote({1}))"), elementsIdenticalTo(c("{")));
    assertThat( eval("class(quote(if(TRUE) 1 else 0))"), elementsIdenticalTo(c("if")));
    assertThat( eval("class(quote(while(TRUE) 1))"), elementsIdenticalTo(c("while")));
    assertThat( eval("class(quote(for(x in 1:9) x))"), elementsIdenticalTo(c("for")));
 //   assertThat( eval("class(quote(x=1)"), equalTo(c("=")));
    assertThat( eval("class(quote(x<-1)"), elementsIdenticalTo(c("<-")));
    assertThat( eval("class(quote((1+1))"), elementsIdenticalTo(c("(")));
  }
  
  @Test
  public void implicitClassesAreOverridenByClassAttribute() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    eval("class(m) <- c('foo','bar')");
    assertThat( eval("class(m)"), elementsIdenticalTo(c("foo", "bar")));
  }

  @Test
  public void matricesHaveImplicitClass() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    assertThat( eval("class(m)"), elementsIdenticalTo(c("matrix")));
  }
  
  @Test
  public void matricesAreNotObjects() {
    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    assertThat( eval("is.object(m)"), elementsIdenticalTo(c(false)));
  }
  
  @Test
  public void arraysHaveImplicitClass() {
    eval("a <- 1:12");
    eval("dim(a) <- 12");
    assertThat( eval("class(a)"), elementsIdenticalTo(c("array")));
  }
  
  
  
  @Test
  public void unclass() {
    eval("x<-1");
    eval("class(x) <- 'foo'");
    eval("x <- unclass(x)");
    assertThat(eval("class(x)"), elementsIdenticalTo(c("numeric")));
  }
  
  @Test
  public void unclassPreservesOtherAttribs() {
    eval("x<-1");
    eval("attr(x,'zing')<-'bat'");
    eval("class(x) <- 'foo'");
    eval("x <- unclass(x)");
    assertThat(eval("class(x)"), elementsIdenticalTo(c("numeric")));
    assertThat(eval("attr(x,'zing')"), elementsIdenticalTo(c("bat")));

  }

  @Test
  public void setNamesWithNonStrVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- c(4,5,6) ");

    assertThat( eval("names(x)"), elementsIdenticalTo( c("4", "5","6")));
  }

  @Test
  public void setNamesWithNonVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- quote(quote(z)) ");

    assertThat( eval("names(x)"), elementsIdenticalTo( c("z", StringVector.NA, StringVector.NA)));
  }

  @Test
  public void setAttributes() {
    eval(" x <- 1:5");
    eval(" attributes(x) <- list(names=c('a','b', 'c'), foo='bar') ");

    assertThat(eval(" names(x) "), elementsIdenticalTo(c("a", "b", "c", StringVector.NA, StringVector.NA)));
    assertThat( eval(" attr(x, 'foo') "), elementsIdenticalTo( c("bar")));

  }

  @Test
  public void asEnvironment() {
    assertThat( eval("as.environment(1)"), sameInstance((SEXP) topLevelContext.getGlobalEnvironment()));
    assertThat( eval("as.environment(2)"), sameInstance((SEXP) topLevelContext.getGlobalEnvironment().getParent()));
  }

  @Test
  public void asVector() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");

    assertThat( eval("as.vector(1, 'character')"), elementsIdenticalTo( c("1" )));
    assertThat( eval("as.vector(c(4,5,0), mode='logical')"), elementsIdenticalTo(c(true, true, false)));
    assertThat( eval("as.vector(c(TRUE,FALSE,NA), mode='double')"), elementsIdenticalTo(c(1.0, 0, DoubleVector.NA)));
  }
  
  @Test
  public void asVectorDropsNames() {
    eval("x <- c(Intercept=1, x=2)");
    eval("attr(x, 'foo') <- 'bar'");
    eval("y <- as.vector(x)");
    
    assertThat( eval("attributes(y)"), identicalTo((SEXP)Null.INSTANCE));
  }

  @Test
  public void asVectorPreservesNamesForLists() {
    eval("x <- c(a=1, b=2, c=3)");
    eval("attr(x, 'foo') <- 'bar'");
    
    eval("y <- as.vector(x, 'list') ");

    // Names are preserved
    assertThat(eval("names(y)"), elementsIdenticalTo(c("a", "b", "c")));
    
    // all other attributes are saved
    assertThat(eval("attr(y, 'foo')"), identicalTo((SEXP)Null.INSTANCE));
  }

  @Test
  public void asVectorPreservesNamesForPairListsButNothingElse() {
    eval("x <- c(a=1, b=2, c=3)");
    eval("attr(x, 'foo') <- 'bar'");

    eval("y <- as.vector(x, 'pairlist') ");

    // Names are preserved
    assertThat(eval("names(y)"), elementsIdenticalTo(c("a", "b", "c")));

    // all other attributes are saved
    assertThat(eval("attr(y, 'foo')"), identicalTo((SEXP)Null.INSTANCE));
  }

  @Test
  public void pairListAsPairListPreservesAllAttributes() {
    eval("x <- pairlist(1,2,3,4)");
    eval("dim(x) <- c(2,2) ");
    eval("attr(x, 'foo') <- 'bar'");

    eval("y <- as.vector(x, 'pairlist')");
    assertThat(eval("dim(y)"), elementsIdenticalTo(c_i(2, 2)));
    assertThat(eval("attr(y, 'foo')"), elementsIdenticalTo(c("bar")));
    
    
    eval("y <- as.pairlist(x) ");
    assertThat(eval("dim(y)"), elementsIdenticalTo(c_i(2, 2)));
    assertThat(eval("attr(y, 'foo')"), elementsIdenticalTo(c("bar")));
  }


  @Test
  public void asVectorDoesNotPreserveAttributesForLists() {
    eval("x <- 1:12");
    eval("dim(x) <- c(2,6)");
    eval("rownames(x) <- c('a', 'b')");
    
    eval("y <- as.vector(x, 'list') ");

    // Dims and dimnames are NOT preserved
    assertThat(eval("dim(y)"), identicalTo((SEXP)Null.INSTANCE));
    assertThat(eval("dimnames(y)"), identicalTo((SEXP)Null.INSTANCE));
  }
  
  @Test
  public void naSymbol() {
    eval(" s <- .Internal(as.vector('NA', 'symbol'))");
    assertTrue(global.getVariable(topLevelContext, "s").equals(Symbol.get("NA")));
  }


  @Test(expected = EvalException.class)
  public void zeroLengthSymbol() {
    eval(".Internal(as.vector('', 'symbol'))");
  }


  @Test
  public void asPairList() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");
    eval(" x <- as.vector( c(a=1,b=2), mode = 'pairlist') ");

    PairList.Node head = (PairList.Node) global.getVariable(topLevelContext, "x");
    assertThat( head.length(), equalTo(2));
    assertThat( head.getNode(0).getTag(), identicalTo( symbol("a")));
    assertThat( head.getElementAsSEXP(0), elementsIdenticalTo( c(1) ));
    assertThat(head.getNode(1).getTag(), identicalTo( symbol("b") ));
    assertThat( head.getElementAsSEXP(1), elementsIdenticalTo( c(2) ));
  }

  @Test
  public void options() {
    eval(" .Internal(options(foo=TRUE)) ");
  }

  @Test
  public void pairListToList() {

    eval(" x <- .Internal(as.vector(list(a=41, b=42), 'pairlist')) ");
    eval(" y <- .Internal(as.vector(x, 'list')) ");

    assertThat( eval("y"), elementsIdenticalTo( list(41d,42d)));
    assertThat( eval("names(x)"), elementsIdenticalTo( c("a", "b")));
    assertThat( eval(".Internal(typeof(x))"), elementsIdenticalTo( c("pairlist")));
    assertThat( eval("names(y)"), elementsIdenticalTo( c("a", "b")));
  }

  @Test
  public void functionCallToList() {

    eval(" x <- quote(~(0+births)) ");
    eval(" y <- .Internal(as.vector(x, 'list')) ");

    assertThat( eval("length(y)"), elementsIdenticalTo( c_i(2)));
    assertThat( eval("names(y)"), identicalTo(  NULL ));
    assertThat( eval(".Internal(typeof(y[[2]]))"), elementsIdenticalTo( c("language")));
  }
  
  @Test
  public void setLength(){
    eval("x <- c(1,2,3)");
    assertThat(eval("length(x)"), elementsIdenticalTo(c_i(3)));
    eval("length(x)<-4");
    assertThat(eval("length(x)"), elementsIdenticalTo(c_i(4)));
    assertThat(eval("is.na(x[4])"), elementsIdenticalTo(c(true)));
    
    eval("length(x) <- 2");
    assertThat(eval("x"), elementsIdenticalTo(c(1,2)));
  }
  
  @Test
  public void setLengthOnOneDimensionalNamedArray() {
    eval("a <- 1:3");
    eval("dim(a) <- 3");
    eval("dimnames(a) <- list(letters[1:3])");
    
    eval("length(a) <- 4");
    
    assertThat(eval("dim(a)"), identicalTo(NULL));
    assertThat(eval("names(a)"), elementsIdenticalTo(c("a", "b", "c", "")));

  }

  @Test
  public void setLengthWithNames() {
    eval("x <- c(a=1,b=2,c=3)");
    eval("attr(x, 'foo') <- 'baz'");
    eval("y = c(a=1, b=2, c=3)");
    eval("attr(y, 'foo') <- 'bar'");

    eval("length(x)<-2");
    eval("length(y)<-3");
    assertThat(eval("length(x)"), elementsIdenticalTo(c_i(2)));
    assertThat(eval("length(names(x))"), elementsIdenticalTo(c_i(2)));
    assertThat(eval("attr(x,'foo')"), identicalTo(NULL));

    assertThat(eval("length(y)"), elementsIdenticalTo(c_i(3)));
    assertThat(eval("length(names(y))"), elementsIdenticalTo(c_i(3)));
    assertThat(eval("attr(y,'foo')"), identicalTo(c("bar")));

  }
  
  @Test
  public void isRawAndAsRaw(){
    assertThat( eval("is.raw(as.raw(c(123,124)))"), elementsIdenticalTo(c(Logical.TRUE)));
    assertThat( eval("as.raw(c(1,20,30))"), elementsIdenticalTo(c_raw(0x1, 0x14, 0x1e)));
  }
  
  @Test
  public void rawToBits(){
    assertThat( eval(".Internal(rawToBits(as.raw(c(1,2))))"), identicalTo(bits("1000000001000000")));
  }
  
  @Test
  public void charToRaw(){
    assertThat( eval(".Internal(charToRaw(\"ABC\"))"), elementsIdenticalTo(c_raw(0x41, 0x42, 0x43)));
  }
  
  @Test
  public void multiByteCharToRaw(){
    assertThat( eval(".Internal(charToRaw('\u00a0'))"), elementsIdenticalTo(c_raw(0xc2, 0xa0)));
  }
  
  @Test
  public void rawShift() {
    assertThat(eval(".Internal(rawShift(as.raw(c(29:31)),1))"), elementsIdenticalTo(c_raw(0x3a, 0x3c, 0x3e)));
  }
  
  @Test
  public void intToBits() {
    assertThat(eval(".Internal(intToBits(1))"), identicalTo(bits("10000000000000000000000000000000")));
    assertThat(eval(".Internal(intToBits(234234))"), identicalTo(bits("01011111010010011100000000000000")));
    assertThat(eval(".Internal(intToBits(NA))"), identicalTo(bits("00000000000000000000000000000001")));

  }

  private SEXP bits(String bits) {
    RawVector.Builder vector = new RawVector.Builder();
    for(int i=0;i!=bits.length();++i) {
      vector.add(bits.charAt(i) == '1' ? 1 : 0);
    }
    return vector.build();
  }

  @Test
  public void isNaGeneric() {
    
    eval("x<-1");
    eval("class(x) <- 'foo'");
    
    eval("is.na.foo <- function(x) 'FOO!!'");
    assertThat(eval("is.na(x)"), elementsIdenticalTo(c("FOO!!")));
  }
  
  @Test
  public void rawToChar() {
    
    byte[] bytes = "!\"#$%&'()".getBytes();
    String s = new String(bytes);
    
    assertThat(eval(".Internal(rawToChar(as.raw(32:126), FALSE))"), elementsIdenticalTo(
        c(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~")));
    
  }
  
  @Test
  public void drop() {
    
    eval("x <- 1:12");
    eval("dim(x) <- c(3,1, 4, 1)");
    eval("dimnames(x) <- dimnames(x) <- list(c('r1','r2','r3'), 'd2', c('c1', 'c2', 'c3', 'c4'), 'd4')");
    eval("y <- .Internal(drop(x))");
    
    assertThat(eval("dim(y)"), elementsIdenticalTo(c_i(3,4)));
    assertThat(eval("dimnames(y)[[1]]"), elementsIdenticalTo(c("r1", "r2", "r3")));
    assertThat(eval("dimnames(y)[[2]]"), elementsIdenticalTo(c("c1", "c2", "c3", "c4")));
  }


  
  
  @Test
  public void expression() {
    eval(" ex <- expression({ x * 2})");
    eval(" x<-4 ");
    assertThat( eval(".Internal(eval(ex,globalenv(),NULL))"), elementsIdenticalTo(c(8)));
  }

  @Test(expected=EvalException.class) 
  public void getThrowsOnNonExistantVariable() {
    eval(".Internal(get('nonexistant.variable', globalenv(), 'any', TRUE))");
  }
  
  @Test
  public void existsNoInherit() {

    eval("x <- 42");
    eval("f <- function() { exists('x', inherits=FALSE) } ");
    assertThat( eval("f()"), elementsIdenticalTo(c(false)));
  }
  
  @Test
  public void getNoInheritThrows() {

    eval("x <- 42");
    eval("f <- function() { exists('x', inherits=FALSE) } ");
    eval("f()");
  }
  
  @Test
  public void listToEnvironment() {
    eval("x <- as.environment(list(a=42,b=64))");
    assertThat(eval("x$a"), elementsIdenticalTo(c(42)));
    assertThat(eval("x$b"), elementsIdenticalTo(c(64)));

  }
  
  @Test
  public void asEnvironmentWithName() {
    assertThat(eval("as.environment('package:base')"), 
        is((SEXP)topLevelContext.getBaseEnvironment()));
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

  @Test
  public void asListDropsDimension() {
    eval("x <- list(1,2,3,4,5,6)");
    eval("dim(x) <- 2:3 ");
    
    eval("y <- .Internal(as.vector(x, 'list'))");
    
    assertThat(eval("dim(y)"), identicalTo((SEXP)Null.INSTANCE));
  }
  
}
