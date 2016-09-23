package org.renjin.sexp;

import org.junit.Test;
import org.renjin.parser.NumericLiterals;

import static org.junit.Assert.assertTrue;

public class NumericLiteralsTest {

  @Test
  public void testParseDouble() {

    assertTrue("Can parse simple literal", NumericLiterals.parseDouble("2") == 2);
    assertTrue("Ignores whitespace at beginning", NumericLiterals.parseDouble(" 2") == 2);
    assertTrue("Ignores whitespace at end", NumericLiterals.parseDouble("2 ") == 2);
    assertTrue("Can parse simple literal with sign", NumericLiterals.parseDouble("-2") == -2);
    assertTrue("Can parse NA", Double.isNaN(NumericLiterals.parseDouble("NA")));
    assertTrue("Can parse decimal", NumericLiterals.parseDouble("2.4") == 2.4);
    assertTrue("Can parse exponent", NumericLiterals.parseDouble("2.4e5") == 2.4e5);
    assertTrue("Can parse hexadecimal", NumericLiterals.parseDouble("0x5") == 0x5);
    assertTrue("Can parse infinity", NumericLiterals.parseDouble("Inf") == Double.POSITIVE_INFINITY);

    // Test cases that cannot be parsed and thus return NaN
    assertTrue("Illegal character return NaN", Double.isNaN(NumericLiterals.parseDouble("a")));
    assertTrue("Whitespace in literal returns NaN", Double.isNaN(NumericLiterals.parseDouble("4 2")));
    assertTrue("Whitespace in hexadecimals returns NaN", Double.isNaN(NumericLiterals.parseDouble("0xep2 2")));
  }

}
