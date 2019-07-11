package org.renjin.primitives.text;

import org.junit.Test;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FormatInfoTest {

  @Test
  public void testInteger() {
    DoubleVector x = new DoubleArrayVector(123);

    FormatInfo info = new FormatInfo(x);
    assertThat(info.getWidth(), equalTo(3));
    assertThat(info.getFractionDigits(), equalTo(0));
    assertThat(info.getExponentDigits(), equalTo(0));
  }

  @Test
  public void testPi() {
    FormatInfo info = new FormatInfo(new DoubleArrayVector(Math.PI));
    assertThat(info.getWidth(), equalTo(8));
    assertThat(info.getFractionDigits(), equalTo(6));
    assertThat(info.getExponentDigits(), equalTo(0));
  }

  @Test
  public void testExp() {
    FormatInfo info = new FormatInfo(new DoubleArrayVector(1e8));
    assertThat(info.getWidth(), equalTo(5));
    assertThat(info.getFractionDigits(), equalTo(0));
    assertThat(info.getExponentDigits(), equalTo(1));
  }

  @Test
  public void testBigExp() {
    FormatInfo info = new FormatInfo(new DoubleArrayVector(1e222));
    assertThat(info.getWidth(), equalTo(6));
    assertThat(info.getFractionDigits(), equalTo(0));
    assertThat(info.getExponentDigits(), equalTo(2));
  }
}