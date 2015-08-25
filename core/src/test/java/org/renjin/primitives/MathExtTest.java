package org.renjin.primitives;


import org.junit.Test;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MathExtTest {

  @Test
  public void roundTest() {
    assertThat(MathExt.round(7.0, 20), equalTo(7.0));
    assertThat(MathExt.round(7.0, 1), equalTo(7.0));
    assertThat(MathExt.round(7.3234324, 0), equalTo(7.0));
    assertThat(MathExt.round(1d/3d, 5), equalTo(0.33333));
    
    assertThat(MathExt.round(7.*Math.pow(10,20)), equalTo(7e20));
  }
  
  @Test
  public void signifTest() {
    assertThat(MathExt.signif(Math.PI, 2), equalTo(3.1d));
    assertThat(MathExt.signif(Math.PI, 3), equalTo(3.14d));
    assertThat(MathExt.signif(Math.PI, 1), equalTo(3.0d));
    assertThat(MathExt.signif(Math.PI, 0), equalTo(3.0d));
    assertThat(MathExt.signif(Math.PI, -1), equalTo(3.0d));
  }

  @Test
  public void signifInfiniteTest() {
    assertThat(MathExt.signif(Double.POSITIVE_INFINITY, 2), equalTo(Double.POSITIVE_INFINITY));
    assertThat(MathExt.signif(Double.NEGATIVE_INFINITY, 2), equalTo(Double.NEGATIVE_INFINITY));
    assertTrue(DoubleVector.isNA(MathExt.signif(DoubleVector.NA, 3)));
  }
}
