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
  
  @Ignore("not implemented")
  @Test
  public void zeroWidthNegativeLookAheadAssertion() throws RESyntaxException {
    //  x = c("~", "c(y1, y2, y3)", "u"), split = "~(?![^\\(].*\\))", out = list("", "c(y1, y2, y3)", "u")
    ExtendedRE re = new ExtendedRE("~(?![^\\(].*\\))");
  //  x = c("~", "y", "1"), split = "~(?![^\\(].*\\))", out = list("", "y", "1")
  }
}
