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

package r.lang;

import org.junit.Before;
import org.junit.Test;
import r.lang.exception.EvalException;
import r.parser.ParseOptions;
import r.parser.ParseState;
import r.parser.RLexer;
import r.parser.RParser;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.ExpMatchers.logicalVectorOf;
import static r.ExpMatchers.realVectorEqualTo;

public class EvalTest {


  private GlobalContext context;

  @Before
  public void setUp() {
    context = new GlobalContext();
  }

  @Test
  public void unaryFunction() throws IOException {
    SEXP result = eval1("sqrt(4);");

    assertThat(result, realVectorEqualTo(2));
  }

  @Test
  public void ifStatement() throws IOException {
    assertThat(eval1("if(TRUE) 1;"), realVectorEqualTo(1));
  }


  @Test
  public void ifStatementWithArgsToBeEvaluated() throws IOException {
    eval1("x<-1;");
    assertThat(eval1("if(x) 9;"), realVectorEqualTo(9));
  }

  @Test
  public void ifElseStatement() throws IOException {
    assertThat(eval1("if(TRUE) 1 else 2;"), realVectorEqualTo(1));
  }

  @Test
  public void ifElseFalseStatement() throws IOException {
    assertThat(eval1("if(FALSE) 1 else 2;"), realVectorEqualTo(2));
  }

  @Test(expected = EvalException.class)
  public void ifWithNA() throws IOException {
    eval1("if(NA) 1;");
  }

  @Test
  public void braces() throws IOException {
    assertThat(eval1("{1; 2};"), realVectorEqualTo(2));
  }

  @Test
  public void emptyBraces() throws IOException {
    assertThat(eval1("{};"), equalTo((SEXP) NilExp.INSTANCE));
  }

  @Test
  public void assign() throws IOException {
    assertThat(eval1("x<-2;"), realVectorEqualTo(2));
    assertThat(eval1("x;"), realVectorEqualTo(2));
  }

  @Test
  public void assignSym() throws IOException {
    eval1("x<-1;");
    eval1("y<-x;");
    assertThat(eval1("y;"), realVectorEqualTo(1));
  }

  @Test
  public void whileLoop() throws IOException {
    eval1("x<-TRUE;");
    eval1("while(x) { x<-FALSE };");

    assertThat(eval1("x;"), logicalVectorOf(Logical.FALSE));
  }

  @Test
  public void whileLoopWithBreak() throws IOException {
    eval1("x<-TRUE;");
    eval1("while(x) { break; x<-FALSE };");

    assertThat(eval1("x;"), logicalVectorOf(Logical.TRUE));
  }

  @Test
  public void simplestForStatement() throws IOException {
    eval1("for( x in 99 ) { y <- x}; ");

    assertThat(eval1("x;"), realVectorEqualTo(99));
    assertThat(eval1("y;"), realVectorEqualTo(99));
  }

  @Test
  public void function() throws IOException {
    eval1("f <- function(x) { x };");
    assertThat(eval1("f(4);"), realVectorEqualTo(4));
  }

  @Test
  public void functionWithMissing() throws IOException {
    eval1("f <- function(x) { missing(x) };");
    assertThat(eval1("f();"), logicalVectorOf(Logical.TRUE));
    assertThat(eval1("f(1);"), logicalVectorOf(Logical.FALSE));
   }



  private SEXP eval1(String source) throws IOException {
    SEXP exp = parse(source);
    SEXP result = exp.evaluate(context.getGlobalEnvironment());
    return result;
  }


  private SEXP parse(String source) throws IOException {
    ParseState state = new ParseState();
    ParseOptions options = ParseOptions.defaults();
    RLexer lexer = new RLexer(context, options, state, new StringReader(source));
    RParser parser = new RParser(options, state, context, lexer);

    assertThat("parser.parse succeeds", parser.parse(), equalTo(true));
    RParser.StatusResult status = parser.getResultStatus();
    return parser.getResult();
  }

}
