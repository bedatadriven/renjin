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
    SEXP result = evalToExp("sqrt(4);");

    assertThat(result, realVectorEqualTo(2));
  }

  @Test
  public void ifStatement() throws IOException {
    assertThat(evalToExp("if(TRUE) 1;"), realVectorEqualTo(1));
  }


  @Test
  public void ifStatementWithArgsToBeEvaluated() throws IOException {
    evalToExp("x<-1;");
    assertThat(evalToExp("if(x) 9;"), realVectorEqualTo(9));
  }

  @Test
  public void ifElseStatement() throws IOException {
    assertThat(evalToExp("if(TRUE) 1 else 2;"), realVectorEqualTo(1));
  }

  @Test
  public void ifElseFalseStatement() throws IOException {
    assertThat(evalToExp("if(FALSE) 1 else 2;"), realVectorEqualTo(2));
  }

  @Test(expected = EvalException.class)
  public void ifWithNA() throws IOException {
    evalToExp("if(NA) 1;");
  }

  @Test
  public void braces() throws IOException {
    assertThat(evalToExp("{1; 2};"), realVectorEqualTo(2));
  }

  @Test
  public void emptyBraces() throws IOException {
    assertThat(evalToExp("{};"), equalTo((SEXP) NilExp.INSTANCE));
  }

  @Test
  public void assign() throws IOException {
    assertThat(evalToExp("x<-2;"), realVectorEqualTo(2));
    assertThat(evalToExp("x;"), realVectorEqualTo(2));
  }

  @Test
  public void assignIsSilent() throws IOException {
    assertThat(eval("x<-1;").isVisible(), equalTo(false));
  }

  @Test
  public void assignSym() throws IOException {
    evalToExp("x<-1;");
    evalToExp("y<-x;");
    assertThat(evalToExp("y;"), realVectorEqualTo(1));
  }

  @Test
  public void whileLoop() throws IOException {
    evalToExp("x<-TRUE;");
    evalToExp("while(x) { x<-FALSE };");

    assertThat(evalToExp("x;"), logicalVectorOf(Logical.FALSE));
  }

  @Test
  public void whileLoopWithBreak() throws IOException {
    evalToExp("x<-TRUE;");
    evalToExp("while(x) { break; x<-FALSE };");

    assertThat(evalToExp("x;"), logicalVectorOf(Logical.TRUE));
  }

  @Test
  public void simplestForStatement() throws IOException {
    evalToExp("for( x in 99 ) { y <- x}; ");

    assertThat(evalToExp("x;"), realVectorEqualTo(99));
    assertThat(evalToExp("y;"), realVectorEqualTo(99));
  }

  @Test
  public void function() throws IOException {
    evalToExp("f <- function(x) { x };");
    assertThat(evalToExp("f(4);"), realVectorEqualTo(4));
  }

  @Test
  public void functionWithMissing() throws IOException {
    evalToExp("f <- function(x) { missing(x) };");
    assertThat(evalToExp("f();"), logicalVectorOf(Logical.TRUE));
    assertThat(evalToExp("f(1);"), logicalVectorOf(Logical.FALSE));
   }

  @Test
  public void functionWithZeroArgs() throws IOException {
    evalToExp("f <- function() { 1 }; ");
    assertThat(evalToExp("f();"), realVectorEqualTo(1));
  }


  private SEXP evalToExp(String source) throws IOException {
    SEXP exp = parse(source);
    return exp.evalToExp(context.getGlobalEnvironment());
  }

  private EvalResult eval(String source) throws IOException {
    SEXP exp = parse(source);
    return exp.evaluate(context.getGlobalEnvironment());
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
