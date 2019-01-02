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

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.junit.Assert.assertThat;


public class DeparseTest extends EvalTestCase {

  @Test
  public void deparse() {
    
    assertThat(eval("deparse(1)"), elementsIdenticalTo(c("1")));
    assertThat(eval("deparse(c(1,2,3,4))"), elementsIdenticalTo(c("c(1, 2, 3, 4)")));
    assertThat(eval("deparse(c(1L,3L,4L))"), elementsIdenticalTo(c("c(1L, 3L, 4L)")));
    assertThat(eval("deparse(c(1L,NA,4L))"), elementsIdenticalTo(c("c(1L, NA, 4L)")));
    assertThat(eval("deparse(c(99L))"), elementsIdenticalTo(c("99L")));
    assertThat(eval("deparse(c(x=99L,y=45L))"), elementsIdenticalTo(c("structure(c(99L, 45L), .Names = c(\"x\", \"y\"))")));
    assertThat(eval("deparse(NA_real_)"), elementsIdenticalTo(c("NA_real_")));
    assertThat(eval("deparse(1:10)"), elementsIdenticalTo(c("1:10")));
    assertThat(eval("deparse(5:1)"), elementsIdenticalTo(c("5:1")));
    assertThat(eval("deparse(list(1,'s',3L))"), elementsIdenticalTo(c("list(1, \"s\", 3L)")));
    assertThat(eval("deparse(list(a=1,b='foo'))"), elementsIdenticalTo(c("structure(list(a = 1, b = \"foo\"), .Names = c(\"a\", \"b\"))")));
  } 
 
  @Test
  public void deparsePrimitives() {
    assertThat(eval("deparse(c)"), elementsIdenticalTo(c(".Primitive(\"c\")")));
    assertThat(eval("deparse(`$`)"), elementsIdenticalTo(c(".Primitive(\"$\")")));
  }
  
  @Test
  public void emptyVectors() {
    assertThat(eval("deparse(c(1L)[-1])"), elementsIdenticalTo(c("integer(0)")));
    assertThat(eval("deparse(c(1)[-1])"), elementsIdenticalTo(c("numeric(0)")));
  }
  
  @Test
  public void deparseBrackets() {
    assertThat(eval("deparse(quote({}))"), elementsIdenticalTo(c("{\n}")));
    assertThat(eval("deparse(quote({1;2;3;}))"), elementsIdenticalTo(c("{\n1\n2\n3\n}")));
  }
  
  @Test
  public void deparseMalformedParens() {
    assertThat(eval("deparse(quote(`(`()))"), elementsIdenticalTo(c("(NULL)")));
  }
  
  @Test
  public void deparseCalls() {
    assertThat(eval("deparse(quote(if(x) y else  z))"), elementsIdenticalTo(c("if (x) y else z")));
    assertThat(eval("deparse(quote(for(i in x) z()))"), elementsIdenticalTo(c("for(i in x) z()")));
    assertThat(eval("deparse(quote(repeat x))"), elementsIdenticalTo(c("repeat x")));
    assertThat(eval("deparse(quote(while(x) y))"), elementsIdenticalTo(c("while (x) y")));
    assertThat(eval("deparse(quote(f(x=1,3)))"), elementsIdenticalTo(c("f(x = 1, 3)")));
    assertThat(eval("deparse(quote({ x+1 }))"), elementsIdenticalTo(c("{\nx + 1\n}")));
    assertThat(eval("deparse(quote(x:y))"), elementsIdenticalTo(c("x:y")));
    assertThat(eval("deparse(quote(2*(1+x)))"), elementsIdenticalTo(c("2 * (1 + x)")));
    assertThat(eval("deparse(quote(!x))"), elementsIdenticalTo(c("!x")));
    assertThat(eval("deparse(quote(x[1,drop=TRUE]))"), elementsIdenticalTo(c("x[1, drop = TRUE]")));
    assertThat(eval("deparse(quote(x[[1]]))"), elementsIdenticalTo(c("x[[1]]")));
    assertThat(eval("deparse(quote(x$y))"), elementsIdenticalTo(c("x$y")));
  }
  
  @Test
  public void noAttributesOnDeparsedCalls() {
    eval("x <- quote(x+y)");
    eval("attr(x,'foo') <- 'bar' ");
    assertThat(eval("deparse(x)"), elementsIdenticalTo(c("x + y")));
    
  }
  
  @Test
  public void deparseWithAttribs() {
    eval("x <- c(a=1,b=2)");
    assertThat(eval("deparse(x)"), elementsIdenticalTo(c("structure(c(1, 2), .Names = c(\"a\", \"b\"))")));

    eval("dim(x) <- c(1L, 2L)");
    eval("attr(x, 'foo') <- 'bar'");
   
    assertThat(eval("deparse(x)"), elementsIdenticalTo(c("structure(c(1, 2), .Dim = 1:2, foo = \"bar\")")));
  }

  @Test
  public void deparseMalformedFunctionCall() {
    eval("x <- quote(c(1))");
    eval("y <- x[-1]");

    assertThat(eval("typeof(y)"), elementsIdenticalTo(c("language")));
    assertThat(eval("length(y)"), elementsIdenticalTo(c_i(1)));
    assertThat(eval("y[[1]]"), elementsIdenticalTo(c(1)));
    assertThat(eval("deparse(y)"), elementsIdenticalTo(c("1()")));
  }

  @Test
  public void deparseFormula() {
    assertThat(eval("deparse(quote(~0+1))"), elementsIdenticalTo(c("~0 + 1")));
    assertThat(eval("deparse(quote(x~y))"), elementsIdenticalTo(c("x ~ y")));
    assertThat(eval("deparse(quote(`~`(1,2,3)))"), elementsIdenticalTo(c("`~`(1, 2, 3)")));
    assertThat(eval("deparse(quote(`~`()))"), elementsIdenticalTo(c("`~`()")));
  }


  @Test
  public void deparseSymbolsWithBackticks() {
    assertThat(eval("deparse(quote(`1`(x,y)))"), elementsIdenticalTo(c("`1`(x, y)")));
    assertThat(eval("deparse(quote(`.1`(x,y)))"), elementsIdenticalTo(c("`.1`(x, y)")));
    assertThat(eval("deparse(quote(`_a`(x,y)))"), elementsIdenticalTo(c("`_a`(x, y)")));
    assertThat(eval("deparse(quote(`a#$#2`(x,y)))"), elementsIdenticalTo(c("`a#$#2`(x, y)")));

  }
  
  @Test
  public void deparseCustomInfix() {
    assertThat(eval("deparse(quote(`%foo%`(1,2)))"), elementsIdenticalTo(c("1 %foo% 2")));

    // Only special formatting if exactly two arguments
    assertThat(eval("deparse(quote(`%foo%`(1)))"), elementsIdenticalTo(c("`%foo%`(1)")));
    assertThat(eval("deparse(quote(`%foo%`(1, 2, 3)))"), elementsIdenticalTo(c("`%foo%`(1, 2, 3)")));
  }
}
