/**
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
package org.renjin.parser;

import org.hamcrest.core.IsEqual;
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


  @Test
  public void testParseDouble2() {

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
    assertTrue("Empty string return NaN", Double.isNaN(NumericLiterals.parseDouble("")));
    assertTrue("Empty string with whitespace return NaN", Double.isNaN(NumericLiterals.parseDouble(" ")));
  }

  @Test
  public void parseNumbersWithWhitespace() {
    assertThat(NumericLiterals.parseDouble("   1.5"), equalTo(1.5));
    assertThat(NumericLiterals.parseDouble("  0.0330   "), equalTo(0.033));
  }

  @Test
  public void scientificNotation() {
    // Comparison values generated by parsing in GNUR3.3.2 and serializing the binary result to disk
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("6.19339507602051")), equalTo(4618659161366518962L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("5.02324111721458e-10")), equalTo(4467925411431961969L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.351900901161679")), equalTo(4600010902987278237L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("2.2082802531306e-12")), equalTo(4432505870791824817L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("1.04305861588421e-09")), equalTo(4472614752319987709L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.072908995231498")), equalTo(4589918067443269435L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("6.30057571608235e-10")), equalTo(4469160773653448643L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("4.83392579838586e-10")), equalTo(4467742316890377477L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("5.96771866662364e-08")), equalTo(4499101508853428449L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.0708165657838894")), equalTo(4589767292011580648L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("7.46840385941552e-05")), equalTo(4545139133774809679L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("1.65419232384833e-10")), equalTo(4460459378064327969L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("8.19674071627272e-05")), equalTo(4545676551518725471L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.0240776327408882")), equalTo(4582597126550172912L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("14.2166568787125")), equalTo(4624192884382436377L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("8.7432183987322e-09")), equalTo(4486366580468022035L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("2.3282039431783e-07")), equalTo(4507891733556365622L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("16.2739730890076")), equalTo(4625273933878347153L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-1.37141509544715e-05")), equalTo(-4689159005496375957L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.001570534915311")), equalTo(4564885636809333732L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("66.5898534286346")), equalTo(4634386261297617618L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-2.2400646196851e-05")), equalTo(-4686139307920392423L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("3.19760041566629e-14")), equalTo(4405083636048736352L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-4.19996957243922e-15")), equalTo(-4831541905846469827L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("1.99307479859504e-13")), equalTo(4416919417898024209L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("1.17676464924164e-10")), equalTo(4458612419207270052L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-5.54835992305972e-15")), equalTo(-4829832618009163797L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("19.213600633273")), equalTo(4626101365472907259L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-3.76945167698697e-12")), equalTo(-4787163648365114152L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("2.48156409889647e-09")), equalTo(4478074483803271835L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.17366164248561")), equalTo(4595424840357629356L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-0.000423825922885238")), equalTo(-4666918204879307102L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-1.00100678871759e-06")), equalTo(-4706038089321373892L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-0.042441074035903")), equalTo(-4637094812452155892L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-1.44254652751862e-13")), equalTo(-4808633486123228578L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("12.0335648340437")), equalTo(4622963912817575639L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-3.36303637957187e-11")), equalTo(-4773115157905289551L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-2.51696884379672e-10")), equalTo(-4759939879962094472L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-0.0158934475075686")), equalTo(-4643133841092897889L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-0.00356290964924414")), equalTo(-4653010104021293745L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-2.75951044079601e-07")), equalTo(-4714559841638382241L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.00388421771911596")), equalTo(4571102818800199944L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-2.91227788947102e-18")), equalTo(-4878844890028647752L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("0.000242938673057609")), equalTo(4553094879071831086L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("6.47489137783496")), equalTo(4618976098026508395L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("4.01587729463698e-13")), equalTo(4421481899296507151L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("5.5628926503489e-16")), equalTo(4378636681542527607L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("3.89151122482512e-08")), equalTo(4495969513984648559L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-6.47209095482353e-11")), equalTo(-4768808067180930263L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("1.04385131324459e-12")), equalTo(4427703735420731486L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("4.42590013997858e-08")), equalTo(4496777059681204646L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("-3.44446728998705e-07")), equalTo(-4713265994731649535L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("1.83730506237278e-06")), equalTo(4521283254470592931L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("1.00076018875072e-16")), equalTo(4367603570519948073L));
    assertThat(Double.doubleToLongBits(NumericLiterals.parseDouble("7.26580778483999")), equalTo(4619866590735475661L));
  }


}
