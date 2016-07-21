package org.renjin.gcc.runtime;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MathlibTest {

  @Test
  public void testFloatingPointModulus() {
    // This should be EXACT EXACT
    assertTrue(Mathlib.fmod(1.5, 1.) == 0.5);
  }
}