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
package org.renjin.primitives.text.regex;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RECompilerTest {

  @Test
  public void posixCharacterClasses1() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^[[:alpha:].]+$");
    assertTrue(re.match("tools"));
    assertTrue(re.match("too.ls"));
    assertFalse(re.match("too9s"));
  }
  

  @Test
  public void dashInCharacterClass() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^[AB-]+$");
    assertTrue(re.match("A--BBBB"));
  }
  
  @Test
  public void upperAndLowerCase() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^[ABab]+$");
    assertTrue(re.match("ABa"));  
  }
  
  @Test
  public void bigCharClass() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE(
        "^" +
        "[AB^-]+" +
        "$");

    assertTrue(re.match("A-B"));
  }
  
  @Test
  public void negateCharClass() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^[^a]+$");
 
    assertTrue(re.match("b334 "));
    assertFalse(re.match("ab"));
    
  }
  
  @Test
  public void caretInCharacterClass() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^[a^]+$");
    assertTrue(re.match("aaaaa"));
    assertTrue(re.match("a^a^a^a"));

    re = new ExtendedRE("^[a^b]+$");
    assertTrue(re.match("a^a^b^a"));

  }
  
  @Test
  public void emailRegexp() throws RESyntaxException {
    // used in tools package
    ExtendedRE re = new ExtendedRE(
        "^(" +
            "\\\".+\\\"|" +
            "([ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%*/?|^{}`~&'+=_-]+\\.)*" +
              "[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%*/?|^{}`~&'+=_-]+" +
    		")" +
    		"@" +
    		"([ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-]+\\.)*" +
    		"[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-]+$");
    assertTrue(re.match("akbertram@gmail.com"));
    assertTrue(re.match("Ak-bertram@gmail.com"));
    assertFalse(re.match("akbertramfoo"));
    assertFalse(re.match("akber tram@foo"));
    assertTrue(re.match("R-core@r-project.org"));

    //    assertTrue(re.match("akbertram@gmail.com"));

  }
  
  @Test
  public void simpleCharClass() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^[Abc]+$");
    assertTrue(re.match("AAAAAAc"));
    assertFalse(re.match("AAAxxAAAc"));
    
  }
  
  @Test
  public void posixCharacterClasses() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^(R|[[:alpha:]][[:alnum:].]*[[:alnum:]])$");
    assertTrue(re.match("tools"));
    assertFalse(re.match("my cool package!"));
    
  }
  
  @Test
  public void compileProblem() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("[[<>=!]+");
    assertTrue(re.match("["));
    assertTrue(re.match(">="));
  }

  @Test
  public void hexDigitsWithBrackets() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("\\x{42}aa");
    assertTrue(re.match("Baa"));
    assertTrue(re.match("Baazzz"));
    assertFalse(re.match("B"));
    assertFalse(re.match("baa"));
  }

  @Test
  public void fourHexDigitsWithBrackets() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("\\x{0042}aa");
    assertTrue(re.match("Baa"));
    assertTrue(re.match("Baazzz"));
    assertFalse(re.match("B"));
    assertFalse(re.match("baa"));  }

  @Test
  public void hexDigitsWithoutBrackets() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("\\x42aa");
    assertTrue(re.match("Baa"));
    assertTrue(re.match("Baazzz"));
    assertFalse(re.match("B"));
    assertFalse(re.match("baa"));
  }

  @Test
  public void nonCapturingGroups() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("(?:foo)?bar");
    assertTrue(re.match("foobar"));
    assertTrue(re.match("bar"));
    assertFalse(re.match("baz"));
  }

  @Test
  public void nullClosureOperand() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("^(?:(?:AB{0,3})*)?C");
    assertTrue(re.match("C"));
    assertFalse(re.match("ZC"));

    assertTrue(re.match("AC"));
    assertTrue(re.match("ABBC"));
    assertTrue(re.match("ABBBC"));
    assertFalse(re.match("ABBBBC"));
  }

  @Test
  public void backslashInCharacterClass() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("[/\\]");

    assertTrue(re.match("\\"));
    assertTrue(re.match("/foo/foo"));
    assertFalse(re.match("foo"));
    assertFalse(re.match(""));
  }

  @Test(expected = RESyntaxException.class)
  public void trailingBackslashError() throws RESyntaxException {
    new ExtendedRE("[A-Z]\\");
  }

  @Test
  public void punctSubClass() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("[[:punct:]]");
    assertTrue(re.match("."));
    assertFalse(re.match("A"));
    assertTrue(re.match("+"));
  }

  @Ignore("not implemented")
  @Test
  public void zeroWidthNegativeLookAheadAssertion() throws RESyntaxException {
    //  x = c("~", "c(y1, y2, y3)", "u"), split = "~(?![^\\(].*\\))", out = list("", "c(y1, y2, y3)", "u")
    ExtendedRE re = new ExtendedRE("~(?![^\\(].*\\))");
  //  x = c("~", "y", "1"), split = "~(?![^\\(].*\\))", out = list("", "y", "1")
  }

  @Ignore("wip")
  @Test
  public void lookahead() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("(\\n|^)(?!$)");
    re.subst("a\nb\nc", "\n     ");
  }

  @Ignore("wip")
  @Test
  public void lookahead2() throws RESyntaxException {
    ExtendedRE re = new ExtendedRE("Hello World([a-z]B)?$");
    REPrettyPrinter prettyPrinter = new REPrettyPrinter();
    re.match("Hello World");
    System.out.println(prettyPrinter.prettyPrint(re.program));
  }

}
