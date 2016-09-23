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

import org.junit.Test;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.renjin.parser.NumericLiterals.parseDouble;

public class NumericLiteralsTest {

  
  @Test
  public void testParseDouble() {
    assertThat(parseDouble("4"), equalTo(4d));
    assertThat(parseDouble("423"), equalTo(423d));
    assertThat(parseDouble("423.5"), equalTo(423.5));
    assertThat(parseDouble("423.025"), equalTo(423.025));
    assertThat(parseDouble("+423.5"), equalTo(423.5));
    assertThat(parseDouble("-4"), equalTo(-4d));
    assertThat(parseDouble("-423"), equalTo(-423d));
    assertThat(parseDouble("-423.5"), equalTo(-423.5));
  }
  
  @Test
  public void positiveInfinity() {
    assertThat(parseDouble("Inf"), equalTo(Double.POSITIVE_INFINITY));
    assertThat(parseDouble("INF"), equalTo(Double.POSITIVE_INFINITY));
    assertThat(parseDouble("inf"), equalTo(Double.POSITIVE_INFINITY));
    assertThat(parseDouble("Infinity"), equalTo(Double.POSITIVE_INFINITY));
    assertThat(parseDouble("INFINITY"), equalTo(Double.POSITIVE_INFINITY));
    assertThat(parseDouble("infinity"), equalTo(Double.POSITIVE_INFINITY));
    assertThat(parseDouble("+inf"), equalTo(Double.POSITIVE_INFINITY));
    assertThat(parseDouble("+Infinity"), equalTo(Double.POSITIVE_INFINITY));

  }
  
  @Test
  public void negativeInfinity() {
    assertThat(parseDouble("-Inf"), equalTo(Double.NEGATIVE_INFINITY));
    assertThat(parseDouble("-INF"), equalTo(Double.NEGATIVE_INFINITY));
    assertThat(parseDouble("-inf"), equalTo(Double.NEGATIVE_INFINITY));    
  }

  @Test
  public void onlyDotIsInvalid() {
    assertTrue(DoubleVector.isNA(parseDouble(".")));

  }
  
  @Test
  public void nan() {
    assertTrue(Double.isNaN(parseDouble("NAN")));
    assertTrue(Double.isNaN(parseDouble("NaN")));
    assertTrue(Double.isNaN(parseDouble("nan")));
  }
  
  @Test
  public void parseDoubleHex() {
    assertThat(parseDouble("0x0"), equalTo(0d));
    assertThat(parseDouble("0x1"), equalTo(1d));
    assertThat(parseDouble("0x9"), equalTo(9d));
    assertThat(parseDouble("0xA"), equalTo(10d));
    assertThat(parseDouble("0xF"), equalTo(15d));
    assertThat(parseDouble("0xa"), equalTo(10d));
    assertThat(parseDouble("0xf"), equalTo(15d));
    assertThat(parseDouble("0xCAFEBABE"), equalTo(3405691582d));
    assertThat(parseDouble("0xcafebabe"), equalTo(3405691582d));
  }
  
  @Test
  public void parseExp() {
    assertThat(parseDouble("5e3"), equalTo(5000d));
    assertThat(parseDouble("5e03"), equalTo(5000d));
    assertThat(parseDouble("5e-02"), equalTo(0.05));


  }
  
  @Test
  public void parseHexWithDecimal() {
    // Apparently the expected behavior is to simply ignore decimal points.
    // Have confirmed that this is the behavior of GNU R
    assertThat(parseDouble("0xAA"), equalTo(170d));
    assertThat(parseDouble("0xA.A"), equalTo(170d));
  }
  
  @Test
  public void parseHexWithExponent() {
    // Apparently the expected behavior is to simply ignore decimal points.
    // Have confirmed that this is the behavior of GNU R
    assertThat(parseDouble("0x1p1"), equalTo(2d));
    assertThat(parseDouble("0x1p2"), equalTo(4d));
    assertThat(parseDouble("0x1p+2"), equalTo(4d));
    assertThat(parseDouble("0x1p-2"), equalTo(0.25d));
    assertThat(parseDouble("0x1p-02"), equalTo(0.25d));
  }


  @Test
  public void parseZeroPointZero() {
    assertThat(parseDouble("0.0"), equalTo(0d));
  }

  @Test
  public void exponent() {
    assertThat(parseDouble("1e+06"), equalTo(1e6));
  }
  
}
