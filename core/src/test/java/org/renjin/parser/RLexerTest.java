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
package org.renjin.parser;

import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.ExpMatchers;
import org.renjin.sexp.Logical;
import org.renjin.sexp.SEXP;

import java.io.Reader;
import java.io.StringReader;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.ExpMatchers.symbolNamed;
import static org.renjin.parser.RParser.*;

public class RLexerTest {

  @Test
  public void simpleInteger() {
    assertTokenSequence("41L", intVector(41));
  }

  @Test
  public void hexInteger() {
    assertTokenSequence("0x10L", intVector(16));
  }

  @Test
  public void nil() {
    assertTokenSequence("NULL", t(NULL_CONST));
  }

  @Test
  public void floatingWithInitialDot() {
    assertTokenSequence(".41", realVector(0.41));
  }

  @Test
  public void expNotation() {
    assertTokenSequence("3.65E45", realVector(3.65E45));
  }

  @Test
  public void expNotationNeg() {
    assertTokenSequence("1e-7", realVector(1e-7));
  }

  @Test
  public void simpleString() {
    assertTokenSequence("\"foo bar\"", strVector("foo bar"));
  }

  @Test
  public void stringWithNewlineEscape() {
    assertTokenSequence("\"\\n\"", strVector("\n"));
  }

  @Test
  public void crlfNewLine() {
    assertTokenSequence("1\r\n3", realVector(1), t(10), realVector(3));
  }

  @Ignore("Cited as a valid example in R language manual but returns error on R.2.10")
  @Test
  public void redundantExponent() {
    assertTokenSequence("2e", realVector(2));
  }

  @Ignore("Cited as a valid example in R language manual but returns error on R.2.10")
  @Test
  public void redundantPosExp() {
    assertTokenSequence("3e+", realVector(3));
  }

  @Test
  public void na() {
    assertTokenSequence("NA", logicalVector(Logical.NA));
  }

  @Test
  public void unaryPlus() {
    assertTokenSequence("+3", t('+'), realVector(3));
  }

  @Test
  public void stringWithOctalEscape() {
    assertTokenSequence("\"\\101\"", strVector("A"));
  }

  @Test
  public void stringWithHexEscape() {
    assertTokenSequence("\"\\x41\"", strVector("A"));
  }

  @Test
  public void stringWithUnicodeEscape() {
    assertTokenSequence("\"\\u41\"", strVector("A"));
  }

  @Test
  public void stringWithDelimitedUnicodeEscape() {
    assertTokenSequence("\"\\u{41}\"", strVector("A"));
  }

  @Test
  public void numberWithWhitespace() {
    assertTokenSequence("   \t3.145  ", realVector(3.145));
  }

  @Test
  public void longNumber() {
    assertTokenSequence("43234234L", intVector(43234234));
  }

  @Test
  public void namedSymbol() {
    assertTokenSequence("x", symbol("x"));
  }

  @Test
  public void symbolAssignedNumber() {
    assertTokenSequence("a<-4", symbol("a"), t(LEFT_ASSIGN), realVector(4));
  }

  @Test
  public void numbersAdded() {
    assertTokenSequence("3.61+99", realVector(3.61), t('+'), realVector(99));
  }

  @Test
  public void extraNewlinesAreIgnored() {
    assertTokenSequence("1+\n5", realVector(1), t('+'), realVector(5));
  }

  @Test
  public void symbolsCorrectlyNamed() {
    assertTokenSequence("+", t('+', symbolNamed("+")));
  }

  @Test
  public void terminatingNewLinesAreIncluded() {
    assertTokenSequence("a \n b \n c", symbol("a"), t('\n'), symbol("b"), t('\n'), symbol("c"));
  }

  @Test
  public void function() {
    assertTokenSequence("function(x) { x * 2}", FUNCTION, '(', SYMBOL, ')', '{', SYMBOL, '*', NUM_CONST, '}');
  }

  @Test
  public void functionWithNewLines() {
    assertTokenSequence("function(x) {\n x * 2\n}", FUNCTION, '(', SYMBOL, ')', '{', SYMBOL, '*', NUM_CONST, '\n', '}');
  }

  @Test
  public void braces() {
    assertTokenSequence("{a}", '{', SYMBOL, '}');
  }

  @Test
  public void bracedExpressionList() {
    assertTokenSequence("{ a\n b }", '{', SYMBOL, '\n', SYMBOL, '}');
  }
  
  @Test
  public void newLinesFollowingElseAreIgnored() {
    assertTokenSequence("if(TRUE) 1 else\n2\n", 
        t(IF), 
        t('('), 
        logicalVector(Logical.TRUE),  
        t(')'), 
        realVector(1), 
        t(ELSE),
        realVector(2),
        t('\n'));
  }

  @Test
  public void bigIfStatement() {
    assertTokenSequence("if( any(x <= 0) ) y <- log(1+x) else y <- log(x)",
        IF,
        '(',
        SYMBOL,
        '(',
        SYMBOL,
        LE,
        NUM_CONST,
        ')',
        ')',
        SYMBOL,
        LEFT_ASSIGN,
        SYMBOL,
        '(',
        NUM_CONST,
        '+',
        SYMBOL,
        ')',
        ELSE,
        SYMBOL,
        LEFT_ASSIGN,
        SYMBOL,
        '(',
        SYMBOL,
        ')');
  }


  private static class LexExpectation {
    public int expectedToken;
    public Matcher<SEXP> sexpMatcher;

    private LexExpectation(int expectedToken, Matcher<SEXP> sexpMatcher) {
      this.expectedToken = expectedToken;
      this.sexpMatcher = sexpMatcher;
    }
  }

  private LexExpectation t(int token) {
    return new LexExpectation(token, ExpMatchers.anyExp());
  }

  private LexExpectation t(int token, Matcher<SEXP> matcher) {
    return new LexExpectation(token, matcher);
  }

  private LexExpectation strVector(final String string) {
    return new LexExpectation(STR_CONST, ExpMatchers.stringVectorOf(string));
  }

  private LexExpectation symbol(final String name) {
    return new LexExpectation(SYMBOL, ExpMatchers.symbolNamed(name));
  }

  private LexExpectation intVector(final int value) {
    return new LexExpectation(NUM_CONST, ExpMatchers.intVectorOf(value));
  }

  private LexExpectation logicalVector(Logical value) {
    return new LexExpectation(NUM_CONST, ExpMatchers.logicalVectorOf(value));
  }


  private LexExpectation realVector(final double value) {
    return new LexExpectation(NUM_CONST, ExpMatchers.realVectorEqualTo(value));
  }

  private void assertTokenSequence(String source, int... tokens) {
    LexExpectation[] list = new LexExpectation[tokens.length];
    for (int i = 0; i != tokens.length; ++i) {
      list[i] = t(tokens[i]);
    }
    assertTokenSequence(source, list);
  }

  private void assertTokenSequence(String source, LexExpectation... expects) {
    Reader reader = new StringReader(source);
    RLexer lexer = new RLexer(ParseOptions.defaults(), new ParseState(), reader);

    for (int i = 0; i != expects.length; ++i) {
      assertThat("token " + (i + 1), lexer.yylex(), equalTo(expects[i].expectedToken));
      assertThat(lexer.getLVal(), expects[i].sexpMatcher);
    }
    assertThat("end of input", lexer.yylex(), equalTo(END_OF_INPUT));
  }
}
