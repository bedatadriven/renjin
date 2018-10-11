/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.parser;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.renjin.ExpMatchers.*;
import org.renjin.EvalTestCase;

import static org.junit.Assert.assertThat;


public class RParserTest {

  @Before
  public void setUp() {
  }

  @Test
  public void one() throws IOException {
    SEXP r = parseSingle("1\n\n");
    assertThat(r, CoreMatchers.instanceOf(DoubleArrayVector.class));
  }
  
  @Test(expected = ParseException.class)
  public void error() throws IOException {
    parseSingle("switch(x, 1= 2= 'foo')");
  }
  
  @Ignore
  @Test
  public void parseWithSourceRefs() throws IOException {
    ExpressionVector sexps = RParser.parseAllSource(new StringReader("x+1\nx+y\n"));
    assertThat(sexps.getAttributes().get(Symbols.SRC_REF), not(is(Null.INSTANCE)));
  }

  @Test
  public void onePlusOne() throws IOException {
    FunctionCall r = (FunctionCall) parseSingle("1 + 1;");
    assertThat(r.length(), equalTo(3));
    assertThat(r.getElementAsSEXP(0), symbolNamed("+"));
    assertThat(r.getElementAsSEXP(1), realVectorEqualTo(1));
    assertThat(r.getElementAsSEXP(2), realVectorEqualTo(1));
  }

  @Test
  public void symbol() throws IOException {
    SEXP s = parseSingle("a;");

    assertThat(s, symbolNamed("a"));
  }

  @Test
  public void na() throws IOException {
    SEXP s = parseSingle("NA;");

    assertThat(s, logicalVectorOf(Logical.NA));
  }

  @Test
  public void assignment() throws IOException {
    FunctionCall r = (FunctionCall) parseSingle("a <- 3;");
    assertThat(r.length(), equalTo(3));
    assertThat(r.getElementAsSEXP(0), symbolNamed("<-"));
    assertThat(r.getElementAsSEXP(1), symbolNamed("a"));
    assertThat(r.getElementAsSEXP(2), realVectorEqualTo(3));
  }

  @Test
  public void exprList() throws IOException {
    SEXP r = parseSingle(" { a<-1; b<-2; a*b } \n");

    System.out.println(r);
  }

  @Test
  public void logical() throws IOException {
    LogicalVector x = (LogicalVector) parseSingle("TRUE\n");
    assertThat(x.length(), equalTo(1));
    assertThat(x.getElementAsInt(0), equalTo(1));
  }

  @Test
  public void functionDef() throws IOException {
    FunctionCall r = (FunctionCall) parseSingle("function (a, b) { a + b }\n");

    assertThat("result length", r.length(), equalTo(4));

    PairList.Node formals = r.getElementAsSEXP(1);
    assertThat(formals.length(), equalTo(2));
    assertThat(formals.getNode(0).getRawTag(), symbolNamed("a"));
    assertThat(formals.getNode(1).getRawTag(), symbolNamed("b"));

    System.out.println(r);
  }

  @Test
  public void functionWithoutArgs() throws IOException {
    FunctionCall r = (FunctionCall) parseSingle("function () { a + b }\n");


  }

  @Test
  public void ifElse() throws IOException {
    FunctionCall r = (FunctionCall) parseSingle("if(TRUE) 1 else 2;");

    System.out.println(r);
  }

  @Test
  public void functionDefWithBodyLength2() throws IOException {
    FunctionCall r = (FunctionCall) parseSingle("function (a, b) { a * b\na + b; }\n");
  }

  @Test
  public void functionDefWithNewlines() throws IOException {
    parseSingle("function (a, b) {  \n " +
        "a + b\n" +
        "}\n");

  }

  @Test
  public void commentsAndLeadingNewLines() throws IOException {
    ExpressionVector s = parseAll("# this is a comment\n\n3.145;");

    assertThat(s.length(), equalTo(1));
    assertThat(s.get(0), realVectorEqualTo(3.145));
  }

  @Test
  public void functionDefWithMultipleExpr() throws IOException {
    String source = "function (a, b) { \n" +
        "x <- a\n" +
        "x <- x * b\n" +
        "x * b\n };";

    System.out.println(source);

    FunctionCall r = (FunctionCall) parseSingle(source);

    System.out.println(r);
  }

  @Test
  public void precededByNewLine() throws IOException {
    ExpressionVector exprList = parseAll("\n1;");

    assertThat(exprList.length(), equalTo(1));
  }
  
  @Test
  public void crOnlyLineEndings() throws IOException {
    parseAll("f <- function(x) {\rc(x)\r}\r");
  }

  @Test
  public void crlfLineEndings() throws IOException {
    parseAll("f <- function(x) {\r\nc(x)\r\n}\r\n");
  }


  @Test
  public void stringWithEscapes() throws IOException {
    StringVector s = (StringVector) parseSingle("\"a\\n\"\n");

    assertThat(s.getElementAsString(0), equalTo("a\n"));
  }

  @Test
  public void parseMultiline() throws IOException {
    ExpressionVector result = parseAll("1\n2\n3\n");

    assertThat(result.length(), equalTo(3));
  }

  @Test
  public void parseWithCommentsPreceding() throws IOException {
    ExpressionVector result = parseAll(
        "# file header\r\n" +
        "\r\n" +
        "x<-function (y) {\r\n" +
        "   y * 2\n" +
        "}\r\n");

    assertThat(result.length(), equalTo(1));
  }
  
  @Test
  public void parseElseWithNewline() throws IOException {
    ExpressionVector result = parseAll("if(TRUE) 1 else\n2\n");
    
    assertThat(result.length(), equalTo(1));
  }
  
  @Test
  public void parseRealScript() throws IOException {
    ExpressionVector result = (ExpressionVector) parseResource("/testScript.R");

    assertThat(result.length(), equalTo(1));
  }
  
  @Ignore
  @Test
  public void parseAddPs() throws IOException {
    ExpressionVector result = (ExpressionVector) parseResource("add.ps.R");

  }
  

  private ExpressionVector parseAll(String source) throws IOException {
    return RParser.parseSource(new StringReader(source));
  }

  private SEXP parseSingle(String source) throws IOException {
    ExpressionVector exp = RParser.parseSource(source);
    return exp.get(0);
  }

  private SEXP parseResource(String source) throws IOException {
    InputStream stream = getClass().getResourceAsStream(source);
    ExpressionVector result = RParser.parseSource(new InputStreamReader(stream));
    stream.close();
    return result;
  }
  
  @Test
  public void matrixProduct() throws IOException{ 
   ExpressionVector result = RParser.parseSource(new StringReader("c(1,2,3) %*% c(7,8,7)\n"));
   FunctionCall call = (FunctionCall)result.getElementAsSEXP(0);
   Symbol function = (Symbol) call.getFunction();
   assertThat(function.getPrintName(), equalTo("%*%")); 
  }

  @Test
  public void testParserLineInformation() throws IOException {
    ExpressionVector result = RParser.parseSource(new StringReader("x"));
    ListVector srcref = (ListVector) ((Vector) result).getAttribute(Symbol.get("srcref"));
    int[] ones = new int[]{1,1,1,1,1,1};
    assertThat(((IntArrayVector) srcref.getElementAsSEXP(0)).toIntArrayUnsafe(), equalTo(ones));
  }

  @Ignore
  public void testParserLineInformation2() throws IOException {
    ExpressionVector result = RParser.parseSource(new StringReader("xy"));
    ListVector srcref = (ListVector) ((Vector) result).getAttribute(Symbol.get("srcref"));
    int[] ones = new int[]{1,1,1,8,1,8};
    assertThat(((IntArrayVector) srcref.getElementAsSEXP(0)).toIntArrayUnsafe(), equalTo(ones));
  }
}
