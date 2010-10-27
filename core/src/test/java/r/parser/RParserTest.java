/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.parser;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import r.lang.*;

import java.io.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.ExpMatchers.*;

public class RParserTest {
  private RParser parser;

  @Test
  public void one() throws IOException {
    SEXP r = parseSingle("1\n");
    assertThat(r, CoreMatchers.instanceOf(RealExp.class));
  }

  @Test
  public void onePlusOne() throws IOException {
    LangExp r = (LangExp) parseSingle("1 + 1;");
    assertThat(r.length(), equalTo(3));
    assertThat(r.get(0), symbolNamed("+"));
    assertThat(r.get(1), realVectorEqualTo(1));
    assertThat(r.get(2), realVectorEqualTo(1));
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
    LangExp r = (LangExp) parseSingle("a <- 3;");
    assertThat(r.length(), equalTo(3));
    assertThat(r.get(0), symbolNamed("<-"));
    assertThat(r.get(1), symbolNamed("a"));
    assertThat(r.get(2), realVectorEqualTo(3));
  }

  @Test
  public void exprList() throws IOException {
    SEXP r = parseSingle(" { a<-1; b<-2; a*b } \n");

    System.out.println(r);
  }

  @Test
  public void logical() throws IOException {
    LogicalExp x = (LogicalExp) parseSingle("TRUE\n");
    assertThat(x.length(), equalTo(1));
    assertThat(x.get(0), equalTo(1));
  }

  @Test
  public void functionDef() throws IOException {
    LangExp r = (LangExp) parseSingle("function (a, b) { a + b }\n");

    assertThat("result length", r.length(), equalTo(4));

    ListExp formals = r.get(1);
    assertThat(formals.length(), equalTo(2));
    assertThat(formals.getNode(0).getTag(), symbolNamed("a"));
    assertThat(formals.getNode(1).getTag(), symbolNamed("b"));

    System.out.println(r);
  }

  @Test
  public void functionWithoutArgs() throws IOException {
    LangExp r = (LangExp) parseSingle("function () { a + b }\n");


  }

  @Test
  public void ifElse() throws IOException {
    LangExp r = (LangExp) parseSingle("if(TRUE) 1 else 2;");

    System.out.println(r);
  }

  @Test
  public void functionDefWithBodyLength2() throws IOException {
    LangExp r = (LangExp) parseSingle("function (a, b) { a * b\na + b; }\n");
  }

  @Test
  public void functionDefWithNewlines() throws IOException {
    parseSingle("function (a, b) {  \n " +
        "a + b\n" +
        "}\n");

  }

  @Test
  public void commentsAndLeadingNewLines() throws IOException {
    ExpExp s = parseAll("# this is a comment\n\n3.145;");

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

    LangExp r = (LangExp) parseSingle(source);

    System.out.println(r);
  }

  @Test
  public void precededByNewLine() throws IOException {
    ExpExp exprList = parseAll("\n1;");

    assertThat(exprList.length(), equalTo(1));

  }

  @Test
  public void parseMultiline() throws IOException {
    ExpExp result = parseAll("1\n2\n3\n");

    assertThat(result.length(), equalTo(3));
  }

  @Test
  public void parseWithCommentsPreceding() throws IOException {
    ExpExp result = parseAll(
        "# file header\r\n" +
        "\r\n" +
        "x<-function (y) {\r\n" +
        "   y * 2\n" +
        "}\r\n");

    assertThat(result.length(), equalTo(1));
  }

  @Test
  public void parseRealScript() throws IOException {
    ExpExp result = (ExpExp) parseResource("/testScript.R");

    assertThat(result.length(), equalTo(1));
  }

  private void createParser(Reader reader) {
    GlobalContext context = new GlobalContext();

    ParseState state = new ParseState();
    ParseOptions options = ParseOptions.defaults();
    RLexer lexer = new RLexer(context, options, state, reader);
    parser = new RParser(options, state, context, lexer);
    parser.setDebugLevel(Integer.MAX_VALUE);
  }

  private ExpExp parseAll(String source) throws IOException {
    return RParser.parseAll(new StringReader(source));
  }

  private SEXP parseSingle(String source) throws IOException {

    createParser(new StringReader(source));
    assertThat("parser.parse succeeds", parser.parse(), equalTo(true));

    RParser.StatusResult status = parser.getResultStatus();
    return parser.getResult();
  }

  private SEXP parseResource(String source) throws IOException {
    InputStream stream = getClass().getResourceAsStream(source);
    createParser(new InputStreamReader(stream));

    return parser.parseAll();
  }

}
