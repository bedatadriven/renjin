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
package org.renjin.gcc.runtime;

/**
 * Implementation of the C Math Library
 */
public class Mathlib {

  public static final double FLT_RADIX = 2.0;
  
  private static final double LN_FLT_RADIX = log(FLT_RADIX);
  
  private static final double LN_2 = log(2);

  public static double acos(double x) {
    return Math.acos(x);
  }

  public static double asin(double x) {
    return Math.asin(x);
  }

  public static double atan(double x) {
    return Math.atan(x);
  }

  public static double atan2(double y, double x) {
    return Math.atan2(y, x);
  }

  public static double cos(double x) {
    return Math.cos(x);
  }

  public static double cosh(double x) {
    return Math.cosh(x);
  }

  public static double sin(double x) {
    return Math.sin(x);
  }
  
  public static double tan(double x) {
    return Math.tan(x);
  }
  
  public static double sinh(double x) {
    return Math.sinh(x);
  }

  public static double tanh(double x) {
    return Math.tanh(x);
  }

  public static double exp(double x) {
    return Math.exp(x);
  }

  public static double expm1(double x) {
    return Math.expm1(x);
  }
  
  /**
   * The returned value is the mantissa and the integer pointed to by exponent is the exponent.
   * The resultant value is x = mantissa * 2 ^ exponent.
   *
   */
  public static double frexp(double value, IntPtr pExponent) {

    // Adapted from:
    // http://stackoverflow.com/questions/1552738/is-there-a-java-equivalent-of-frexp

    long bits = Double.doubleToRawLongBits(value);
    double realMant = 1.;

    // Test for NaN, infinity, and zero.
    if (Double.isNaN(value) ||
        value + value == value ||
        Double.isInfinite(value)) {
      pExponent.set(0);
      return value;

      
    } else {

      boolean neg = (bits < 0);
      int exponent = (int) ((bits >> 52) & 0x7ffL);
      long mantissa = bits & 0xfffffffffffffL;

      if (exponent == 0) {
        exponent++;
      } else {
        mantissa = mantissa | (1L << 52);
      }

      // bias the exponent - actually biased by 1023.
      // we are treating the mantissa as m.0 instead of 0.m
      //  so subtract another 52.
      exponent -= 1075;
      realMant = mantissa;

      // normalize
      while (realMant >= 1.0) {
        mantissa >>= 1;
        realMant /= 2.;
        exponent++;
      }

      if (neg) {
        realMant = realMant * -1;
      }

      pExponent.set((int)exponent);
      return realMant;
    }
  }


  /**
   * The returned value is the mantissa and the integer pointed to by exponent is the exponent. 
   * The resultant value is x = mantissa * 2 ^ exponent.
   */
  public static double ldexp(double x, int d) {
    for (; d > 0; d--) {
      x *= 2.0;
    }
    for (; d < 0; d++) {
      x *= 0.5;
    }
    return x;
  }

  /**
   * @return Returns the natural logarithm (base-e logarithm) of {@code x}
   */
  public static double log(double x) {
    return Math.log(x);
  }

  /**
   * @return Returns the binary (base-2) logarithm of x.
   */
  public static double log2(double x) {
    return Math.log(x) / LN_2;
  }

  /**
   * Returns the logarithm of |x|, using FLT_RADIX as base for the logarithm.
   * 
   * @param x Value whose logarithm is calculated.
   */
  public static double logb(double x) {
    return Math.floor(Math.log(Math.abs(x)) / LN_FLT_RADIX);
  }

  /**
   * @return the common logarithm (base-10 logarithm) of {@code x}.
   */
  public static double log10(double x) {
    return Math.log10(x);
  }
  
  public static double log1p(double x) {
    return Math.log1p(x);
  }
  
  /**
   *  The returned value is the fraction component (part after the decimal), and sets integer to the integer component.
   */
  public static double modf(double x, DoublePtr pInteger) {
    double intValue = rint(x);
    double fracValue = x - intValue;
    pInteger.set(intValue);
    return fracValue;
  }

  public static double pow(double x, double y) {
    return Math.pow(x, y);
  }


  /**
   *   Returns the square root of x.
   */
  public static double sqrt(double x) {
    return Math.sqrt(x);
  }

  public static float sqrtf(float x) {
    return (float) Math.sqrt(x);
  }
  
  /**
   * Returns the cubic root of x
   */
  public static double cbrt(double x) {
    return Math.cbrt(x);
  }

  /**
   * Returns the hypotenuse of a right-angled triangle whose legs are x and y.
   */
  public static double hypot(double x, double y) {
    return Math.hypot(x, y);
  }


  /**
   *   Returns the smallest integer value greater than or equal to x.
   */
  public static double ceil(double x) {
    return Math.ceil(x);
  }

  /**
   * Returns the absolute value of x
   */
  public static double fabs(double x) {
    return Math.abs(x);
  }

  /**
   * Returns the largest integer value less than or equal to x.
   */
  public static double floor(double x) {
    return Math.floor(x);
  }

  /**
   * The nearest integral value that is not larger in magnitude than x (as a floating-point value).
   */
  public static double trunc(double x) {
    if(x > 0) {
      return Math.floor(x);
    } else {
      return Math.ceil(x);
    }
  }

  /**
   * Returns the integral value that is nearest to x, with halfway cases rounded away from zero.
   */
  public static double round(double x) {
    // TODO:
    // Java rounds differently: 
    // "If two double values that are mathematical integers are equally close, 
    // the result is the integer value that is even."

    return Math.rint(x);
  }

  /**
   * Rounds x to an integral value, using the rounding direction specified by fegetround.
   */
  public static double rint(double x) {
    // TODO:
    // Java rounds differently: 
    // "If two double values that are mathematical integers are equally close, 
    // the result is the integer value that is even."

    return Math.rint(x);
  }

  /**
   * Returns the integer value that is nearest in value to x, with halfway cases rounded away from zero.
   */
  public static long lround(double x) {
    return (long)Math.round(x);
  }
  
  

  /**
   * Rounds x to an integral value, using the rounding direction specified by fegetround, and returns 
   * it as a value of type long int.
   */
  public static long lrint(double x) {
    return (long)rint(x);    
  }

  /**
   * Compute remainder of division (function )
   */
  public static double fmod(double x, double y) {
    return x % y;
  }

  /**
   * Maximum value
   */
  public static double fmax(double x, double y) {
    return Math.max(x, y);
  }

  /**
   * Minimum value
   */
  public static double fmin(double x, double y) {
    return Math.min(x, y);
  }


  /**
   * Returns the error function value for x.
   */
  public static double erf(double x) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the complementary error function value for x.
   */
  public static double erfc(double x) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the gamma function of x.
   */
  public static double tgamma(double x) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the natural logarithm of the absolute value of the gamma function of x.
   */
  public static double lgamma(double x) {
    throw new UnsupportedOperationException();
  }

  public static double copysign(double x, double y) {
    return Math.copySign(x, y);
  }
  
  public static float copysignf(float x, float y) {
    return Math.copySign(x, y);
  }
}
