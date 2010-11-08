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

package r.lang;

import org.junit.Test;
import r.lang.exception.EvalException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.ExpMatchers.logicalVectorOf;
import static r.ExpMatchers.realVectorEqualTo;

public class ControlFlowTest extends EvalTestCase {

  @Test
  public void unaryFunction() throws IOException {
    SEXP result = evaluateToExpression("sqrt(4)");

    assertThat(result, realVectorEqualTo(2));
  }

  @Test
  public void ifStatement() throws IOException {
    assertThat(evaluateToExpression("if(TRUE) 1"), realVectorEqualTo(1));
  }

  @Test
  public void ifStatementWithArgsToBeEvaluated() throws IOException {
    evaluate("x<-1");
    assertThat(evaluateToExpression("if(x) 9"), realVectorEqualTo(9));
  }

  @Test
  public void ifElseStatement() throws IOException {
    assertThat(evaluateToExpression("if(TRUE) 1 else 2"), realVectorEqualTo(1));
  }

  @Test
  public void ifElseFalseStatement() throws IOException {
    assertThat(evaluateToExpression("if(FALSE) 1 else 2"), realVectorEqualTo(2));
  }

  @Test(expected = EvalException.class)
  public void ifWithNA() throws IOException {
    evaluateToExpression("if(NA) 1");
  }

  @Test
  public void braces() throws IOException {
    assertThat(evaluateToExpression("{1; 2}"), realVectorEqualTo(2));
  }

  @Test
  public void emptyBraces() throws IOException {
    assertThat(evaluateToExpression("{}"), equalTo((SEXP) NullExp.INSTANCE));
  }

  @Test
  public void assign() throws IOException {
    assertThat(evaluateToExpression("x<-2"), realVectorEqualTo(2));
    assertThat(evaluateToExpression("x"), realVectorEqualTo(2));
  }

  @Test
  public void assignIsSilent() throws IOException {
    assertThat(evaluate("x<-1").isVisible(), equalTo(false));
  }

  @Test
  public void assignSym() throws IOException {
    evaluateToExpression("x<-1");
    evaluateToExpression("y<-x");
    assertThat(evaluateToExpression("y"), realVectorEqualTo(1));
  }

  @Test
  public void whileLoop() throws IOException {
    evaluateToExpression("x<-TRUE");
    evaluateToExpression("while(x) { x<-FALSE }");

    assertThat(evaluateToExpression("x"), logicalVectorOf(Logical.FALSE));
  }

  @Test
  public void whileLoopWithBreak() throws IOException {
    evaluateToExpression("x<-TRUE");
    evaluateToExpression("while(x) { break; x<-FALSE }");

    assertThat(evaluateToExpression("x"), logicalVectorOf(Logical.TRUE));
  }

  @Test
  public void simplestForStatement() throws IOException {
    evaluateToExpression("for( x in 99 ) { y <- x} ");

    assertThat(evaluateToExpression("x"), realVectorEqualTo(99));
    assertThat(evaluateToExpression("y"), realVectorEqualTo(99));
  }

  @Test
  public void function() throws IOException {
    evaluateToExpression("f <- function(x) { x }");
    assertThat(evaluateToExpression("f(4)"), realVectorEqualTo(4));
  }

  @Test
  public void functionWithMissing() throws IOException {
    evaluateToExpression("f <- function(x) { missing(x) }");
    assertThat(evaluateToExpression("f()"), logicalVectorOf(Logical.TRUE));
    assertThat(evaluateToExpression("f(1)"), logicalVectorOf(Logical.FALSE));
   }

  @Test
  public void functionWithZeroArgs() throws IOException {
    evaluateToExpression("f <- function() { 1 } ");
    assertThat(evaluateToExpression("f()"), realVectorEqualTo(1));
  }

}
