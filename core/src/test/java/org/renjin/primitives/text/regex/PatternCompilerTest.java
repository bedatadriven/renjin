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
package org.renjin.primitives.text.regex;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PatternCompilerTest {

  @Test
  public void posixCharacterClasses1() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("^[[:alpha:].]+$");
    assertTrue(re.match("tools"));
    assertTrue(re.match("too.ls"));
    assertFalse(re.match("too9s"));
  }
  

  @Test
  public void dashInCharacterClass() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("^[AB-]+$");
    assertTrue(re.match("A--BBBB"));
  }
  
  @Test
  public void upperAndLowerCase() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("^[ABab]+$");
    assertTrue(re.match("ABa"));  
  }
  
  @Test
  public void bigCharClass() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern(
        "^" +
        "[AB^-]+" +
        "$");

    assertTrue(re.match("A-B"));
  }
  
  @Test
  public void negateCharClass() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("^[^a]+$");
 
    assertTrue(re.match("b334 "));
    assertFalse(re.match("ab"));
    
  }
  
  @Test
  public void caretInCharacterClass() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("^[a^]+$");
    assertTrue(re.match("aaaaa"));
    assertTrue(re.match("a^a^a^a"));

    re = new ExtendedPattern("^[a^b]+$");
    assertTrue(re.match("a^a^b^a"));

  }
  
  @Test
  public void emailRegexp() throws RESyntaxException {
    // used in tools package
    ExtendedPattern re = new ExtendedPattern(
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
    ExtendedPattern re = new ExtendedPattern("^[Abc]+$");
    assertTrue(re.match("AAAAAAc"));
    assertFalse(re.match("AAAxxAAAc"));
    
  }
  
  @Test
  public void posixCharacterClasses() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("^(R|[[:alpha:]][[:alnum:].]*[[:alnum:]])$");
    assertTrue(re.match("tools"));
    assertFalse(re.match("my cool package!"));
    
  }
  
  @Test
  public void compileProblem() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("[[<>=!]+");
    assertTrue(re.match("["));
    assertTrue(re.match(">="));
  }

  @Test
  public void hexDigitsWithBrackets() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("\\x{42}aa");
    assertTrue(re.match("Baa"));
    assertTrue(re.match("Baazzz"));
    assertFalse(re.match("B"));
    assertFalse(re.match("baa"));
  }

  @Test
  public void fourHexDigitsWithBrackets() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("\\x{0042}aa");
    assertTrue(re.match("Baa"));
    assertTrue(re.match("Baazzz"));
    assertFalse(re.match("B"));
    assertFalse(re.match("baa"));  }

  @Test
  public void hexDigitsWithoutBrackets() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("\\x42aa");
    assertTrue(re.match("Baa"));
    assertTrue(re.match("Baazzz"));
    assertFalse(re.match("B"));
    assertFalse(re.match("baa"));
  }

  @Test
  public void nonCapturingGroups() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("(?:foo)?bar");
    assertTrue(re.match("foobar"));
    assertTrue(re.match("bar"));
    assertFalse(re.match("baz"));
  }

  @Test
  public void nullClosureOperand() throws RESyntaxException {
    ExtendedPattern re = new ExtendedPattern("^(?:(?:AB{0,3})*)?C");
    assertTrue(re.match("C"));
    assertFalse(re.match("ZC"));

    assertTrue(re.match("AC"));
    assertTrue(re.match("ABBC"));
    assertTrue(re.match("ABBBC"));
    assertFalse(re.match("ABBBBC"));


  }


  @Ignore("not implemented")
  @Test
  public void zeroWidthNegativeLookAheadAssertion() throws RESyntaxException {
    //  x = c("~", "c(y1, y2, y3)", "u"), split = "~(?![^\\(].*\\))", out = list("", "c(y1, y2, y3)", "u")
    ExtendedPattern re = new ExtendedPattern("~(?![^\\(].*\\))");
  //  x = c("~", "y", "1"), split = "~(?![^\\(].*\\))", out = list("", "y", "1")
  }
}
