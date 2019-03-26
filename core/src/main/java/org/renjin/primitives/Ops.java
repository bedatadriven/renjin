/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives;

import org.apache.commons.math.complex.Complex;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.sexp.*;

import static java.lang.Double.*;
import static java.lang.Math.copySign;


/**
 * Default implementations of the Ops group of functions.
 */
@GroupGeneric
public class Ops  {

  private Ops() {}

  @Deferrable
  @Builtin("+")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double plus(double x, double y) {
    return x + y;
  }


  @Deferrable
  @Builtin("+")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static int plus(@Cast(CastStyle.EXPLICIT) int a, @Cast(CastStyle.EXPLICIT) int b) {
    // check for integer overflow
    try {
      return Math.addExact(a, b);
    } catch(ArithmeticException e) {
      return IntVector.NA;
    }
  }

  @Builtin("+")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static Complex plus(Complex x, Complex y) {
    return x.add(y);
  }

  @Deferrable
  @Builtin("+")
  public static Vector plus(Vector x) {
    return x;
  }

  @Deferrable
  @Builtin("-")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double minus(double x, double y) {
    return x - y;
  }

  @Builtin("-")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static Complex negative(Complex x) {
    return ComplexVector.complex(-x.getReal(), -x.getImaginary());
  }

  @Builtin("-")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static Complex minus(Complex x, Complex y) {
    return new Complex(x.getReal() - y.getReal(),
                       x.getImaginary() - y.getImaginary());
  }

  @Deferrable
  @Builtin("-")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double minus(@Cast(CastStyle.EXPLICIT) double x) {
    return -x;
  }

  @Deferrable
  @Builtin("-")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static int minus(@Cast(CastStyle.EXPLICIT) int x) {
    return -x;
  }

  @Deferrable
  @Builtin("-")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static int minus(@Cast(CastStyle.EXPLICIT) int a, @Cast(CastStyle.EXPLICIT) int b) {
    try {
      return Math.subtractExact(a, b);
    } catch (ArithmeticException e) {
      return IntVector.NA;
    }
  }



  @Deferrable
  @Builtin("/")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double divide(double x, double y) {
    return x / y;
  }

  @Builtin("/")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static Complex divide(Complex dividend, Complex divisor) {
    // LICENSE: transcribed code from GCC, which is licensed under GPL
    // libgcc2 - Translated by Tomas Kalibera
    // The Apache Commons math version does not handle edge cases
    // exactly the same as R/GCC does.

    double a = dividend.getReal();
    double b = dividend.getImaginary();
    double c = divisor.getReal();
    double d = divisor.getImaginary();

    double x;
    double y;

    if (Math.abs(c) < Math.abs(d)) {
      double ratio = c / d;
      double denominator = (c * ratio) + d;
      x = ((a * ratio) + b) / denominator;
      y = ((b * ratio) - a) / denominator;
    } else {
      double ratio = d / c;
      double denominator = (d * ratio) + c;
      x = ((b * ratio) + a) / denominator;
      y = (b - (a * ratio)) / denominator;
    }

    if (isNaN(x) && isNaN(y)) {
      if (c == 0.0 && d == 0.0 && (!isNaN(a) || !isNaN(b))) {
        x = copySign(Double.POSITIVE_INFINITY, c) * a;
        y = copySign(Double.POSITIVE_INFINITY, c) * b;

      } else if ((isInfinite(a) || isInfinite(b)) && isFinite(c) && isFinite(d)) {
        double ra = convertInf(a);
        double rb = convertInf(b);
        x = Double.POSITIVE_INFINITY * (ra * c + rb * d);
        y = Double.POSITIVE_INFINITY * (rb * c - ra * d);

      } else if ((isInfinite(c) || isInfinite(d)) && isFinite(a) && isFinite(b)) {
        double rc = convertInf(c);
        double rd = convertInf(d);
        x = 0.0 * (a * rc + b * rd);
        y = 0.0 * (b * rc - a * rd);
      }
    }
    return ComplexVector.complex(x, y);
  }


  @Deferrable
  @Builtin("*")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double multiply(double x, double y) {
    return x * y;
  }


  @Deferrable
  @Builtin("*")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static int multiply(@Cast(CastStyle.EXPLICIT) int x, @Cast(CastStyle.EXPLICIT) int y) {
    try {
      return Math.multiplyExact(x, y);
    } catch (ArithmeticException e) {
      return IntVector.NA;
    }
  }


  @Builtin("*")
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static Complex multiply(Complex x, Complex y) {
    // LICENSE: transcribed code from GCC, which is licensed under GPL
    // libgcc2 - Adapted by Tomas Kalibera
    // The Apache Commons math version does not handle edge cases
    // exactly the same as R/GCC does.

    double a = x.getReal();
    double b = x.getImaginary();
    double c = y.getReal();
    double d = y.getImaginary();

    double ac = a * c;
    double bd = b * d;
    double bc = b * c;
    double ad = a * d;

    double real = ac - bd;
    double imag = bc + ad;

    if (Double.isNaN(real) && Double.isNaN(imag)) {
      boolean recalc = false;
      double ra = a;
      double rb = b;
      double rc = c;
      double rd = d;
      if (Double.isInfinite(ra) || Double.isInfinite(rb)) {
        ra = convertInf(ra);
        rb = convertInf(rb);
        rc = convertNaN(rc);
        rd = convertNaN(rd);
        recalc = true;
      }
      if (Double.isInfinite(rc) || Double.isInfinite(rd)) {
        rc = convertInf(rc);
        rd = convertInf(rd);
        ra = convertNaN(ra);
        rb = convertNaN(rb);
        recalc = true;
      }
      if (!recalc && (Double.isInfinite(ac) || Double.isInfinite(bd) || Double.isInfinite(ad) || Double.isInfinite(bc))) {
        ra = convertNaN(ra);
        rb = convertNaN(rb);
        rc = convertNaN(rc);
        rd = convertNaN(rd);
        recalc = true;
      }
      if (recalc) {
        real = Double.POSITIVE_INFINITY * (ra * rc - rb * rd);
        imag = Double.POSITIVE_INFINITY * (ra * rd + rb * rc);
      }
    }
    return ComplexVector.complex(real, imag);
  }

  private static double convertNaN(double d) {
    if (Double.isNaN(d)) {
      return Math.copySign(0, d);
    } else {
      return d;
    }
  }

  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo( String x, String y) {
    return x.equals(y);
  }

  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo(Complex x, Complex y) {
    return x.equals(y);
  }

  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo(double x, double y) {
    return x == y;
  }

  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo(int x, int y) {
    return x == y;
  }

  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo(boolean x, boolean y) {
    return x == y;
  }

  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo(byte x, byte y) {
    return x == y;
  }

  @Deferrable
  @Builtin("!=")
  @DataParallel
  public static boolean notEqualTo( String x, String y) {
    return !x.equals(y);
  }

  @Deferrable
  @Builtin("!=")
  @DataParallel
  public static boolean notEqualTo( Complex x, Complex y) {
    return !x.equals(y);
  }


  @Deferrable
  @Builtin("!=")
  @DataParallel
  public static boolean notEqualTo(double x, double y) {
    return x != y;
  }

  @Deferrable
  @Builtin("!=")
  @DataParallel
  public static boolean notEqualTo(int x, int y) {
    return x != y;
  }

  @Deferrable
  @Builtin("!=")
  @DataParallel
  public static boolean notEqualTo(boolean x, boolean y) {
    return x != y;
  }

  @Deferrable
  @Builtin("!=")
  @DataParallel
  public static boolean notEqualTo(byte x, byte y) {
    return x != y;
  }

  @Deferrable
  @Builtin("<")
  @DataParallel
  public static boolean lessThan( String x, String y) {
    return x.compareTo(y) < 0;
  }

  @Builtin("<")
  @DataParallel
  public static boolean lessThan(Complex x, Complex y) {
    throw new EvalException("invalid comparison with complex values");
  }

  @Deferrable
  @Builtin("<")
  @DataParallel
  public static boolean lessThan(double x, double y) {
    return x < y;
  }

  @Deferrable
  @Builtin("<")
  @DataParallel
  public static boolean lessThan(int x, int y) {
    return x < y;
  }

  @Deferrable
  @Builtin("<")
  @DataParallel
  public static boolean lessThan(boolean x, boolean y) {
    return !x && y;
  }

  @Deferrable
  @Builtin("<")
  @DataParallel
  public static boolean lessThan(byte x, byte y) {
    return (x & 0xFF) < (y & 0xFF);
  }

  @Deferrable
  @Builtin("<=")
  @DataParallel
  public static boolean lessThanOrEqualTo(String x, String y) {
    return x.compareTo(y) <= 0;
  }

  @Builtin("<=")
  @DataParallel
  public static boolean lessThanOrEqualTo(Complex x, Complex y) {
    throw new EvalException("invalid comparison with complex values");
  }

  @Deferrable
  @Builtin("<=")
  @DataParallel
  public static boolean lessThanOrEqualTo(double x, double y) {
    return x <= y;
  }

  @Deferrable
  @Builtin("<=")
  @DataParallel
  public static boolean lessThanOrEqualTo(int x, int y) {
    return x <= y;
  }

  @Deferrable
  @Builtin("<=")
  @DataParallel
  public static boolean lessThanOrEqualTo(boolean x, boolean y) {
    return y || !x;
  }

  @Deferrable
  @Builtin("<=")
  @DataParallel
  public static boolean lessThanOrEqualTo(byte x, byte y) {
    return (x & 0xFF) <= (y & 0xFF);
  }

  @Deferrable
  @Builtin(">")
  @DataParallel
  public static boolean greaterThan( String x,  String y) {
    return x.compareTo(y) > 0;
  }

  @Builtin(">")
  @DataParallel
  public static boolean greaterThan(Complex x, Complex y) {
    throw new EvalException("invalid comparison with complex values");
  }

  @Deferrable
  @Builtin(">")
  @DataParallel
  public static boolean greaterThan(double x, double y) {
    return x > y;
  }

  @Deferrable
  @Builtin(">")
  @DataParallel
  public static boolean greaterThan(int x, int y) {
    return x > y;
  }

  @Deferrable
  @Builtin(">")
  @DataParallel
  public static boolean greaterThan(boolean x, boolean y) {
    return x && !y;
  }

  @Deferrable
  @Builtin(">")
  @DataParallel
  public static boolean greaterThan(byte x, byte y) {
    return (x & 0xFF) > (y & 0xFF);
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual( String x,  String y) {
    return x.compareTo(y) >= 0;
  }

  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(Complex x, Complex y) {
    throw new EvalException("invalid comparison with complex values");
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(double x, double y) {
    return x >= y;
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(int x, int y) {
    return x >= y;
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(boolean x, boolean y) {
    return x || !y;
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(byte x, byte y) {
    return (x & 0xFF) >= (y & 0xFF);
  }

  @Deferrable
  @Builtin("^")
  @DataParallel(value=PreserveAttributeStyle.ALL, passNA = true)
  public static double power(double a, double b) {
    // LICENSE: transcribed code from GNU R, which is licensed under GPL

    // NOTE: Math.pow (which uses FDLIBM) is very slow, the version written in assembly in
    // GLIBC (SSE2 optimized) is about 2x faster
    // Thanks to Tomas Kalibera for the adaptation

    // arithmetic.c (GNU R)
    if (b == 2) {
      return a * a;
    }
    if (b == -1) {
      return 1d/a;
    }
    if (a == 1 || b == 0) {
      return 1;
    }
    if (a == 0) {
      if (b > 0) {
        return 0;
      }
      if (b < 0) {
        return Double.POSITIVE_INFINITY;
      }
      return b;  // NA or NaN
    }

    double result = Math.pow(a, b);

    if(Double.isNaN(result)) {
      if(DoubleVector.isNA(a) || DoubleVector.isNA(b)) {
        return DoubleVector.NA;
      }
    }

    if (isFinite(a) && isFinite(b)) {
      return Math.pow(a, b);
    }
    if (Double.isNaN(a) || Double.isNaN(b)) {
      if(DoubleVector.isNA(a) || DoubleVector.isNA(b)) {
        return DoubleVector.NA;
      } else {
        return a + b;
      }
    }
    if (!isFinite(a)) {
      if (a > 0) { // Inf ^ y
        if (b < 0) {
          return 0;
        }
        return Double.POSITIVE_INFINITY;
      } else if (isFinite(b) && b == Math.floor(b)) { // (-Inf) ^ n
        if (b < 0) {
          return 0;
        }
        return fmod(b, 2) != 0 ? a : -a;
      }
    }
    if (!isFinite(b)) {
      if (a >= 0) {
        if (b > 0) {
          return (a >= 1) ? Double.POSITIVE_INFINITY : 0;
        }
        return (a < 1) ? Double.POSITIVE_INFINITY : 0;
      }
    }
    return Double.NaN;
  }

  @Deferrable
  @Builtin("^")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex power(Complex x, Complex y) {
    // handle common cases with better precision than the log(exp(x)*y) trick used by Apache Commons
    if(y.getImaginary() == 0) {
      double yr = y.getReal();
      if(yr == 0) {
        return ComplexVector.complex(1, 0);
      } else if (yr == 1) {
        return x;
      } else {
        int k = (int)yr;
        if(k == yr && k < 65536) {
          return power(x, k);
        }
      }
    }

    return x.pow(y);
  }

  /**
   * Raise a complex number to an integer power
   * @param x the complex number
   * @param k the power to raise the complex number
   * @return  the result
   */
  private static Complex power(Complex x, int k) {
    if(k < 0) {
      return reciprocal(power(x, -k));
    } else {
      Complex result = ComplexVector.complex(1, 0);
      while(k > 0) {
        result = multiply(result, x);
        k--;
      }
      return result;
    }
  }


  private static Complex reciprocal(Complex value) {
    // LICENSE: this code is derived from the division code,
    // which is transcribed code from GCC, which is licensed under GPL

    double c = value.getReal();
    double d = value.getImaginary();

    double x;
    double y;
    if (Math.abs(c) < Math.abs(d)) {
      double ratio = c / d;
      double denominator = (c * ratio) + d;
      x = ratio / denominator;
      y = -1 / denominator;
    } else {
      double ratio = d / c;
      double denominator = (d * ratio) + c;
      x = 1 / denominator;
      y = -ratio / denominator;
    }

    if (isNaN(x) && isNaN(y)) {
      if (c == 0.0 && d == 0.0) {
        x = copySign(Double.POSITIVE_INFINITY, c);
        y = copySign(Double.NaN, c);
      } else if (isInfinite(c) || isInfinite(d)) {
        double rc = convertInf(c);
        double rd = convertInf(d);
        x = 0.0 * rc;
        y = 0.0 * (-rd);
      }
    }
    return ComplexVector.complex(x, y);
  }


  public static double convertInf(double d) {
    return copySign(isInfinite(d) ? 1 : 0, d);
  }

  @Deferrable
  @Builtin("!")
  @DataParallel
  public static boolean not(boolean value) {
    return !value;
  }

  @Builtin("!")
  @DataParallel
  public static byte not(byte value) {
    return (byte)~value;
  }

  @Deferrable
  @Builtin("%%")
  @DataParallel
  public static double modulus(double x, double y) {
    return fmod(x, y);
  }

  @Deferrable
  @Builtin("%%")
  @DataParallel
  public static int modulus(@Cast(CastStyle.EXPLICIT) int x, @Cast(CastStyle.EXPLICIT) int y) {
    if (y != 0) {
      if (x >= 0 && y > 0) {
        return x % y;
      } else {
        return (int) fmod(x, y);
      }
    } else {
      return IntVector.NA;
    }
  }


  public static double fmod(double a, double b) {
    double quotient = a / b;
    if (b != 0d) {
      double tmp = a - Math.floor(quotient) * b;
      if (isFinite(quotient) && Math.abs(quotient) > (1d / DoubleVector.EPSILON)) {
        // TODO: RContext.warning(ast, RError.ACCURACY_MODULUS);
      }
      return tmp - Math.floor(tmp / b) * b;
    } else {
      return Double.NaN;
    }
  }

  @Deferrable
  @Builtin("%/%")
  @DataParallel
  public static double integerDivision(double x, double y) {
    return Math.floor(x / y);
  }

  @Deferrable
  @Builtin("%/%")
  @DataParallel
  public static int integerDivision(@Cast(CastStyle.EXPLICIT) int x, @Cast(CastStyle.EXPLICIT) int y) {
    if (y != 0) {
      return (int) Math.floor((double) x / (double) y);
    } else {
      return IntVector.NA;
    }
  }

  @Generic
  @Deferrable
  @Builtin("&")
  @DataParallel(passNA = true)
  public static Logical and(@DownCastComplex double x, @DownCastComplex double y) {
    if(x == 0 || y == 0) {
      return Logical.FALSE;
    } else if(DoubleVector.isNA(x) || DoubleVector.isNA(y)) {
      return Logical.NA;
    } else {
      return Logical.TRUE;
    }
  }

  @Generic
  @Deferrable
  @Builtin("|")
  @DataParallel(passNA = true)
  public static Logical or(@DownCastComplex double x, @DownCastComplex double y) {
    if( (x != 0 && !DoubleVector.isNA(x)) ||
        (y != 0 && !DoubleVector.isNA(y))) {
      return Logical.TRUE;
    } else if(x == 0 && y == 0) {
      return Logical.FALSE;
    } else {
      return Logical.NA;
    }
  }

  @Generic
  @Builtin("|")
  @DataParallel
  public static byte or(byte x, byte y) {
    return (byte)(x | y);
  }

  @Generic
  @Builtin("&")
  @DataParallel
  public static byte and(byte x, byte y) {
    return (byte)(x & y);
  }
}
