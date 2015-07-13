package org.renjin.gnur;

import org.junit.Test;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.*;
import static org.renjin.gnur.RenjinCApi.R_pow_di;

public class RenjinCApiTest {

  @Test
  public void pow_di() {
    assertThat(R_pow_di(0d, 1), equalTo(0d));
    assertTrue(DoubleVector.isNA(R_pow_di(DoubleVector.NA, 41)));

    assertThat(R_pow_di(2d, 16), equalTo(65536d));
    assertThat(R_pow_di(2d, -16), closeTo(1.525879e-05, 0.0001));
    assertThat(R_pow_di(3d, 7), equalTo(2187d));
    assertThat(R_pow_di(3d, -7), closeTo(0.0004572474d, 0.000000001));
  }
}