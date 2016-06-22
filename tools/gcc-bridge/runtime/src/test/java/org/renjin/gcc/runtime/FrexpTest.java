package org.renjin.gcc.runtime;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.renjin.gcc.runtime.DoubleMatchers.*;


public class FrexpTest {
  private static final int MAX_EXP = Double.MAX_EXPONENT;
  private static final int MIN_EXP = Double.MIN_EXPONENT;
  private static final double MIN_NORMAL_EXP = Double.MIN_NORMAL;

  // Adapted from: https://github.com/gagern/gnulib/blob/master/tests/test-frexp.c
  // Licensed under GPL
  
  
  private void check(double x, Matcher<Double> mantissaMatcher, Matcher<? super Integer> exponentMatcher) {
    
    IntPtr pExponent = new IntPtr(-9999);
    
    double mantissa = Mathlib.frexp(x, pExponent);
    
    String call = String.format("frexp(%f)", x);
    
    assertThat("mantissa of " + call, mantissa, mantissaMatcher);
    assertThat("exponent of " + call, pExponent.unwrap(), exponentMatcher);
  }
  
  public void check(double x, Matcher<Double> mantissaMatcher) {
    check(x, mantissaMatcher, anything());
  }

  @Test
  public void testNaN() {
    check(Double.NaN, isNaN(), anything());
  }
  
  @Test
  public void positiveInfinity() {
    check(Double.POSITIVE_INFINITY, equalTo(Double.POSITIVE_INFINITY));
  }

  @Test
  public void negativeInfinity() {
    check(Double.NEGATIVE_INFINITY, equalTo(Double.NEGATIVE_INFINITY));
  }
  
  @Test
  public void positiveZero() {
    check(0.0, isPositiveZero(), equalTo(0));
  }
  
  @Test
  public void negativeZero() {
    check(-0.0, isNegativeZero(), equalTo(0));
  }

  @Test
  public void exhaustive() {
    int i;
    double x;
    for (i = 1, x = 1.0; i <= MAX_EXP; i++, x *= 2.0)
    {
      check(x, equalTo(0.5), equalTo(i));
    }
    for (i = 1, x = 1.0; i >= MIN_NORMAL_EXP; i--, x *= 0.5)
    {
      check(x, equalTo(0.5), equalTo(i));
    }
    for (; i >= MIN_EXP - 100 && x > 0.0; i--, x *= 0.5)
    {
      check(x, equalTo(0.5), equalTo(i));
    }

    for (i = 1, x = - 1.0; i <= MAX_EXP; i++, x *= 2.0)
    {
      check(x, equalTo(-0.5), equalTo(i));
    }
    for (i = 1, x = - 1.0; i >= MIN_NORMAL_EXP; i--, x *= 0.5)
    {
      check(x, equalTo(-0.5), equalTo(i));
    }
    for (; i >= MIN_EXP - 100 && x < 0.0; i--, x *= 0.5)
    {
      check(x, equalTo(-0.5), equalTo(i));
    }

    for (i = 1, x = 1.01; i <= MAX_EXP; i++, x *= 2.0)
    {
      check(x, equalTo(0.505), equalTo(i));
    }
    
    for (i = 1, x = 1.01; i >= MIN_NORMAL_EXP; i--, x *= 0.5)
    {
      check(x, equalTo(0.505), equalTo(i));
    }
    for (; i >= MIN_EXP - 100 && x > 0.0; i--, x *= 0.5)
    {
      IntPtr exp = new IntPtr(-9999);
      double mantissa = Mathlib.frexp(x, exp);
      
      assertThat(mantissa, greaterThanOrEqualTo(0.5));
      assertThat(mantissa, lessThan(1.0));
      assertThat(mantissa, equalTo(Mathlib.ldexp(x, -exp.unwrap())));
    }

    for (i = 1, x = 1.73205; i <= MAX_EXP; i++, x *= 2.0)
    {
      check(x, equalTo(0.866025), equalTo(i));
    }
    for (i = 1, x = 1.73205; i >= MIN_NORMAL_EXP; i--, x *= 0.5)
    {
      check(x, equalTo(0.866025), equalTo(i));
    }
  }
}