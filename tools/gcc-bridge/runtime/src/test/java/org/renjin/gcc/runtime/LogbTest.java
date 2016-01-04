package org.renjin.gcc.runtime;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the logb function
 */
public class LogbTest {
  
  @Test
  public void test() {
    assertThat(Mathlib.logb(0), equalTo(Double.NEGATIVE_INFINITY));
    assertThat(Mathlib.logb(2), equalTo(1.0));
    assertThat(Mathlib.logb(-2), equalTo(1.0));
    assertThat(Mathlib.logb(4294967171d), equalTo(31.0));
  }
}
