package org.renjin.primitives.text.regex;

import org.junit.Test;
import static org.junit.Assert.*;

public class RECompilerTest {

  @Test
  public void posixCharacterClasses1() {
    ExtendedRE re = new ExtendedRE("^[[:alpha:].]+$");
    assertTrue(re.match("tools"));
    assertTrue(re.match("too.ls"));
    assertFalse(re.match("too9s"));
  }
  

  @Test
  public void dashInCharacterClass() {
    ExtendedRE re = new ExtendedRE("^[AB-]+$");
    assertTrue(re.match("A--BBBB"));
  }
  
  @Test
  public void upperAndLowerCase() {
    ExtendedRE re = new ExtendedRE("^[ABab]+$");
    assertTrue(re.match("ABa"));  
  }
  
  @Test
  public void bigCharClass() {
    ExtendedRE re = new ExtendedRE(
        "^" +
        "[AB^-]+" +
        "$");

    assertTrue(re.match("A-B"));
  }
  
  @Test
  public void negateCharClass() {
    ExtendedRE re = new ExtendedRE("^[^a]+$");
 
    assertTrue(re.match("b334 "));
    assertFalse(re.match("ab"));
    
  }
  
  @Test
  public void caretInCharacterClass() {
    ExtendedRE re = new ExtendedRE("^[a^]+$");
    assertTrue(re.match("aaaaa"));
    assertTrue(re.match("a^a^a^a"));

    re = new ExtendedRE("^[a^b]+$");
    assertTrue(re.match("a^a^b^a"));

  }
  
  @Test
  public void emailRegexp() {
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
  public void simpleCharClass() {
    ExtendedRE re = new ExtendedRE("^[Abc]+$");
    assertTrue(re.match("AAAAAAc"));
    assertFalse(re.match("AAAxxAAAc"));
    
  }
  
  @Test
  public void posixCharacterClasses() {
    ExtendedRE re = new ExtendedRE("^(R|[[:alpha:]][[:alnum:].]*[[:alnum:]])$");
    assertTrue(re.match("tools"));
    assertFalse(re.match("my cool package!"));
    
  }
  
}
