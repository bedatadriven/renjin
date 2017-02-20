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

import org.junit.Before;
import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class DeparseTest extends EvalTestCase {

  @Test
  public void deparse() {
    
    assertThat(eval("deparse(1)"), equalTo(c("1")));
    assertThat(eval("deparse(c(1,2,3,4))"), equalTo(c("c(1, 2, 3, 4)")));
    assertThat(eval("deparse(c(1L,3L,4L))"), equalTo(c("c(1L, 3L, 4L)")));
    assertThat(eval("deparse(c(1L,NA,4L))"), equalTo(c("c(1L, NA, 4L)")));
    assertThat(eval("deparse(c(99L))"), equalTo(c("99L")));
    assertThat(eval("deparse(c(x=99L,y=45L))"), equalTo(c("structure(c(99L, 45L), .Names = c(\"x\", \"y\"))")));
    assertThat(eval("deparse(NA_real_)"), equalTo(c("NA_real_")));
    assertThat(eval("deparse(1:10)"), equalTo(c("1:10")));
    assertThat(eval("deparse(5:1)"), equalTo(c("5:1")));
    assertThat(eval("deparse(list(1,'s',3L))"), equalTo(c("list(1, \"s\", 3L)")));
    assertThat(eval("deparse(list(a=1,b='foo'))"), equalTo(c("structure(list(a = 1, b = \"foo\"), .Names = c(\"a\", \"b\"))")));
  } 
 
  @Test
  public void deparsePrimitives() {
    assertThat(eval("deparse(c)"), equalTo(c(".Primitive(\"c\")")));
    assertThat(eval("deparse(`$`)"), equalTo(c(".Primitive(\"$\")")));
  }
  
  @Test
  public void emptyVectors() {
    assertThat(eval("deparse(c(1L)[-1])"), equalTo(c("integer(0)")));
    assertThat(eval("deparse(c(1)[-1])"), equalTo(c("numeric(0)")));
  }
  
  @Test
  public void deparseBrackets() {
    assertThat(eval("deparse(quote({}))"), equalTo(c("{\n}")));
    assertThat(eval("deparse(quote({1;2;3;}))"), equalTo(c("{\n1\n2\n3\n}")));
  }
  
  @Test
  public void deparseMalformedParens() {
    assertThat(eval("deparse(quote(`(`()))"), equalTo(c("(NULL)")));
  }
  
  @Test
  public void deparseCalls() {
    assertThat(eval("deparse(quote(if(x) y else  z))"), equalTo(c("if (x) y else z")));
    assertThat(eval("deparse(quote(for(i in x) z()))"), equalTo(c("for(i in x) z()")));
    assertThat(eval("deparse(quote(repeat x))"), equalTo(c("repeat x")));
    assertThat(eval("deparse(quote(while(x) y))"), equalTo(c("while (x) y")));
    assertThat(eval("deparse(quote(f(x=1,3)))"), equalTo(c("f(x = 1, 3)")));
    assertThat(eval("deparse(quote({ x+1 }))"), equalTo(c("{\nx + 1\n}")));
    assertThat(eval("deparse(quote(x:y))"), equalTo(c("x:y")));
    assertThat(eval("deparse(quote(2*(1+x)))"), equalTo(c("2 * (1 + x)")));
    assertThat(eval("deparse(quote(!x))"), equalTo(c("!x")));
    assertThat(eval("deparse(quote(x[1,drop=TRUE]))"), equalTo(c("x[1, drop = TRUE]")));
    assertThat(eval("deparse(quote(x[[1]]))"), equalTo(c("x[[1]]")));
    assertThat(eval("deparse(quote(x$y))"), equalTo(c("x$y")));
  }
  
  @Test
  public void noAttributesOnDeparsedCalls() {
    eval("x <- quote(x+y)");
    eval("attr(x,'foo') <- 'bar' ");
    assertThat(eval("deparse(x)"), equalTo(c("x + y")));
    
  }
  
  @Test
  public void deparseWithAttribs() {
    eval("x <- c(a=1,b=2)");
    assertThat(eval("deparse(x)"), equalTo(c("structure(c(1, 2), .Names = c(\"a\", \"b\"))")));

    eval("dim(x) <- c(1L, 2L)");
    eval("attr(x, 'foo') <- 'bar'");
   
    assertThat(eval("deparse(x)"), equalTo(c("structure(c(1, 2), .Dim = 1:2, foo = \"bar\")")));
  }

  @Test
  public void deparseMalformedFunctionCall() {
    eval("x <- quote(c(1))");
    eval("y <- x[-1]");

    assertThat(eval("typeof(y)"), equalTo(c("language")));
    assertThat(eval("length(y)"), equalTo(c_i(1)));
    assertThat(eval("y[[1]]"), equalTo(c(1)));
    assertThat(eval("deparse(y)"), equalTo(c("1()")));
  }

  @Test
  public void deparseFormula() {
    assertThat(eval("deparse(quote(~0+1))"), equalTo(c("~0 + 1")));
    assertThat(eval("deparse(quote(x~y))"), equalTo(c("x ~ y")));
    assertThat(eval("deparse(quote(`~`(1,2,3)))"), equalTo(c("`~`(1, 2, 3)")));
    assertThat(eval("deparse(quote(`~`()))"), equalTo(c("`~`()")));
  }


  @Test
  public void deparseSymbolsWithBackticks() {
    assertThat(eval("deparse(quote(`1`(x,y)))"), equalTo(c("`1`(x, y)")));
    assertThat(eval("deparse(quote(`.1`(x,y)))"), equalTo(c("`.1`(x, y)")));
    assertThat(eval("deparse(quote(`_a`(x,y)))"), equalTo(c("`_a`(x, y)")));
    assertThat(eval("deparse(quote(`a#$#2`(x,y)))"), equalTo(c("`a#$#2`(x, y)")));

  }
  
  @Test
  public void deparseCustomInfix() {
    assertThat(eval("deparse(quote(`%foo%`(1,2)))"), equalTo(c("1 %foo% 2")));

    // Only special formatting if exactly two arguments
    assertThat(eval("deparse(quote(`%foo%`(1)))"), equalTo(c("`%foo%`(1)")));
    assertThat(eval("deparse(quote(`%foo%`(1, 2, 3)))"), equalTo(c("`%foo%`(1, 2, 3)")));
  }
}
