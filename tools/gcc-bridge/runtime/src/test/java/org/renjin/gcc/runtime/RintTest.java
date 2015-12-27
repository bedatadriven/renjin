package org.renjin.gcc.runtime;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.gcc.runtime.DoubleMatchers.isNegativeZero;
import static org.renjin.gcc.runtime.DoubleMatchers.isPositiveZero;


public class RintTest {

  @Test
  public void zero() {
    assertThat(Mathlib.rint(0.0), isPositiveZero());
    assertThat(Mathlib.rint(-0.0), isNegativeZero());
  }
  
  @Test
  public void positive() {
    assertThat(Mathlib.rint(0.200000), equalTo(0.0));
    assertThat(Mathlib.rint(0.500000), equalTo(0.0));
    assertThat(Mathlib.rint(0.700000), equalTo(1.0));
    assertThat(Mathlib.rint(1.0), equalTo(1.0));
    assertThat(Mathlib.rint(2.300000), equalTo(2.0));
    assertThat(Mathlib.rint(2.500000), equalTo(2.0));
    assertThat(Mathlib.rint(2.700000), equalTo(3.0));
  }
  
  @Test
  public void negativeNumbers() {
    assertThat(Mathlib.rint(-0.2), equalTo(-0.0));
    assertThat(Mathlib.rint(-0.5), equalTo(-0.0));
    assertThat(Mathlib.rint(-0.75), equalTo(-1.0));
    assertThat(Mathlib.rint(-2.3), equalTo(-2.0));
    assertThat(Mathlib.rint(-2.5), equalTo(-2.0));
    assertThat(Mathlib.rint(-2.7), equalTo(-3.0));
  }

}
