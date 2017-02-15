/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.apache.commons.math.util.MathUtils;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.repackaged.guava.math.IntMath;
import org.renjin.sexp.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


/**
 * Members of the "Math" generic group functions
 */
@GroupGeneric("Math")
public class MathGroup {
  
  private static final int DBL_MAX_10_EXP = 308;
  
  private static final int MAX_DIGITS = DBL_MAX_10_EXP;

  private MathGroup() {
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double gamma(double x) {
    return org.renjin.nmath.gamma.gammafn(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double lgamma(double x) {
    return org.renjin.nmath.lgamma.lgammafn(x);
  }

  
  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double digamma(double x) {
    return org.renjin.nmath.polygamma.digamma(x);
  }


  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double trigamma(double x) {
    return org.renjin.nmath.polygamma.trigamma(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double sign(double x) {
    return Math.signum(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double log(double x, double base) {
    return MathUtils.log(base, x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double log(double d) {
    return Math.log(d);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double log2(double d) {
    return MathUtils.log(2, d);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double log10(double d) {
    return Math.log10(d);
  }
  
  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double abs(@Cast(CastStyle.EXPLICIT) double x) {
    return Math.abs(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static int abs(@Cast(CastStyle.EXPLICIT) int x) {
    return Math.abs(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double abs(Complex x) {
    return x.abs();
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double sqrt(double x) {
    return Math.sqrt(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double sin(double x) {
    return Math.sin(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double sinh(double x) {
    return Math.sinh(x);
  }


  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double sinpi(double x) {
    return org.renjin.nmath.cospi.sinpi(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double asin(double val) {
    return Math.asin(val);
  }
  
  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double asinh(double val) {
    if(Double.isInfinite(val)) {
      return val;
    }
    return Math.log(val + Math.sqrt(val * val + 1));
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double cos(double val) {
    return Math.cos(val);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double cosh(double val) {
    return Math.cosh(val);
  }


  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double cospi(double x) {
    return org.renjin.nmath.cospi.cospi(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double acos(double val) {
    return Math.acos(val);
  }
  
  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double acosh(double val) {
    return Math.log(val + Math.sqrt(val + 1) * Math.sqrt(val - 1));
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double atanh(double val) {
    return 0.5 * Math.log((1 + val) / (1 - val));
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double tan(double x) {
    return Math.tan(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double tanh(double x) {
    return Math.tanh(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double tanpi(double x) {
    return org.renjin.nmath.cospi.tanpi(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double atan(double x) {
    return Math.atan(x);
  }
  
  @Deferrable
  @Internal
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double atan2(double y, double x) {
    return Math.atan2(y, x);
  }


  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double signif(double x) {
    return signif(x, 6);
  }


  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double signif(double x, int digits) {
    if(Double.isInfinite(x) || Double.isNaN(x)) {
      return x;
    }
    if(digits <= 0) {
      digits = 1;
    }
    return new BigDecimal(x).round(new MathContext(digits, RoundingMode.HALF_UP)).doubleValue();
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex exp(Complex x) {
    return x.exp();
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double exp(double x) {
    return Math.exp(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double expm1(double x) {
    return Math.expm1(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double log1p(double x) {
    return Math.log1p(x);
  }


  @Builtin
  @Deferrable
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double floor(double x) {
    return Math.floor(x);
  }
  
  @Builtin
  @Deferrable
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double ceiling(double x) {
    return Math.ceil(x);
  }
  
  
  @Builtin
  @Deferrable
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double round(double x) {
    return Math.rint(x);
  }
  
  @Builtin
  @Deferrable
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double round(double x, int digits) {
    // adapted from the nmath library, fround.c

    double sign;
    int dig;

    if (Double.isNaN(x) || Double.isNaN(digits)) {
      return x + digits;
    }
    if(!DoubleVector.isFinite(x)) {
      return x;
    }

    if(digits == Double.POSITIVE_INFINITY) {
      return x;
    } else if(digits == Double.NEGATIVE_INFINITY) {
      return 0.0;
    }

    if (digits > MAX_DIGITS) {
      digits = MAX_DIGITS;
    }
    dig = (int)Math.floor(digits + 0.5);

    if(x < 0.) {
      sign = -1.;
      x = -x;
    } else {
      sign = 1.;
    }
    if (dig == 0) {
      return sign * Math.rint(x);
    } else if (dig > 0) {
      // round to a specific number of decimal places
      return sign * new BigDecimal(x).setScale(digits, RoundingMode.HALF_EVEN).doubleValue();
    } else {
      // round to the nearest power of 10
      double pow10 = Math.pow(10., -dig);
      return sign * Math.rint(x/pow10) * pow10;
    }
  }

  @Builtin
  @Deferrable
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex round(Complex x, int digits) {
    return ComplexVector.complex(round(x.getReal(), digits), round(x.getImaginary(), digits));
  }


  @Deferrable
  @Builtin
  @DataParallel(value = PreserveAttributeStyle.ALL, passNA = true)
  public static double trunc(double x) {
    if(x < 0) {
      return Math.ceil(x);
    } else {
      return Math.floor(x);
    }
  }


  @Builtin
  public static DoubleVector cumsum(DoubleVector source) {
    return cumulativeRealSum(source);
  }
  
  @Builtin
  public static DoubleVector cumsum(RawVector source) {
    return cumulativeRealSum(source);
  }

  @Builtin
  public static IntVector cumsum(@Current Context context, IntVector source) {
    return cumulativeIntegerSum(source);
  }

  @Builtin
  public static ComplexVector cumsum(ComplexVector source) {
    ComplexArrayVector.Builder result = new ComplexArrayVector.Builder(source.length());
    result.setAttribute(Symbols.NAMES, source.getNames());
    Complex sum = ComplexVector.complex(0);
    for (int i = 0; i < source.length(); i++) {
      if(source.isElementNA(i)) {
        break;
      }
      sum = sum.add(source.getElementAsComplex(i));
      result.set(i, sum);
    }
    return result.build();
  }

  @Builtin
  public static IntVector cumsum(@Current Context context, LogicalVector source) {
    return cumulativeIntegerSum(source);
  }

  @Builtin
  public static DoubleVector cumsum(StringVector source) {
    return cumulativeRealSum(source);
  }

  @Builtin
  public static DoubleVector cumsum(Null x) {
    return DoubleVector.EMPTY;
  }

  private static DoubleVector cumulativeRealSum(Vector source) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder(source.length());
    result.setAttribute(Symbols.NAMES, source.getNames());
    double sum = 0;
    for (int i = 0; i < source.length(); i++) {
      sum += source.getElementAsDouble(i);
      if (Double.isNaN(sum)) {
        break;
      }
      result.set(i, sum);
    }
    return result.build();
  }


  private static IntVector cumulativeIntegerSum(Vector source) {
    IntArrayVector.Builder result = new IntArrayVector.Builder(source.length());
    result.setAttribute(Symbols.NAMES, source.getNames());
    int sum = 0;
    for (int i = 0; i < source.length(); i++) {
      int x = source.getElementAsInt(i);
      if(x == IntVector.NA) {
        // remaining elements are initialized to NA
        break;
      }
      try {
        sum = IntMath.checkedAdd(sum, x);
      } catch (ArithmeticException e) {
//        context.warn("integer overflow in 'cumsum'; use 'cumsum(as.numeric(.))");
        break;
      }
      result.set(i, sum);
    }
    return result.build();
  }


  @Builtin
  public static DoubleVector cumprod(Null source) {
    return cumulativeRealProduct(source);
  }

  @Builtin
  public static DoubleVector cumprod(StringVector source) {
    return cumulativeRealProduct(source);
  }

  @Builtin
  public static DoubleVector cumprod(IntVector source) {
    return cumulativeRealProduct(source);
  }

  @Builtin
  public static DoubleVector cumprod(LogicalVector source) {
    return cumulativeRealProduct(source);
  }
  
  @Builtin
  public static DoubleVector cumprod(DoubleVector source) {
    return cumulativeRealProduct(source);
  }

  private static DoubleVector cumulativeRealProduct(Vector source) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder();
    result.setAttribute(Symbols.NAMES, source.getNames());

    if(source.length() > 0) {
      double prod = source.getElementAsDouble(0);
      result.add(prod);
      for (int i = 1; i < source.length(); i++) {
        prod *= source.getElementAsDouble(i);
        if (Double.isNaN(prod)) {
          result.addNA();
        } else {
          result.add(prod);
        }
      }
    }
    return result.build();
  }


  @Builtin
  public static ComplexVector cumprod(ComplexVector source) {
    ComplexArrayVector.Builder result = new ComplexArrayVector.Builder();
    result.setAttribute(Symbols.NAMES, source.getNames());

    if(source.length() > 0) {
      Complex prod = source.getElementAsComplex(0);
      result.add(prod);
      for (int i = 1; i < source.length(); i++) {
        if(ComplexVector.isNA(prod)) {
          result.add(ComplexVector.NA);
        } else {
          prod = prod.multiply(source.getElementAsComplex(i));
          result.add(prod);
        }
      }
    }
    return result.build();
  }

  @Builtin
  public static DoubleVector cummax(DoubleVector source) {
    return cumulativeDoubleExtrema(source, false);
  }

  @Builtin
  public static IntVector cummax(LogicalVector source) {
    return cumulativeIntegerExtrema(source, false);
  }
  
  @Builtin
  public static DoubleVector cummax(StringVector source) {
    return cumulativeDoubleExtrema(source, false);
  }

  @Builtin
  public static IntVector cummax(IntVector source) {
    return cumulativeIntegerExtrema(source, false);
  }

  @Builtin
  public static DoubleVector cummin(DoubleVector source) {
    return cumulativeDoubleExtrema(source, true);
  }

  @Builtin
  public static IntVector cummin(LogicalVector source) {
    return cumulativeIntegerExtrema(source, true);
  }

  @Builtin
  public static DoubleVector cummin(StringVector source) {
    return cumulativeDoubleExtrema(source, true);
  }

  @Builtin
  public static IntVector cummin(IntVector source) {
    return cumulativeIntegerExtrema(source, true);
  }

  @Builtin
  public static ComplexVector cummin(ComplexVector source) {
    return cumulativeExtrema("cummin", source);
  }

  @Builtin
  public static ComplexVector cummax(ComplexVector source) {
    return cumulativeExtrema("cummax", source);
  }
  
  @Builtin
  public static DoubleVector cummin(Null x) {
    return DoubleVector.EMPTY;
  }
  
  @Builtin
  public static DoubleVector cummax(Null x) {
    return DoubleVector.EMPTY;
  }
  
  private static ComplexVector cumulativeExtrema(String functionName, ComplexVector source) {
    // This is probably not intended behavior, but cumxxx(complex(0)) in GNU R
    // returns complex(0), so we'll mimic it here
    if(source.length() == 0) {
      if(source.getNames() == Null.INSTANCE) {
        return ComplexVector.EMPTY;
      } else {
        return ComplexVector.NAMED_EMPTY;
      }
    } else {
      throw new EvalException(String.format("'%s' not defined for complex numbers", functionName));
    }
  }

  private static DoubleVector cumulativeDoubleExtrema(Vector source, boolean min) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder(0, source.length());
    result.setAttribute(Symbols.NAMES, source.getNames());

    if(source.length() > 0) {
      double extrema = source.getElementAsDouble(0);
      result.add(extrema);
      for (int i = 1; i < source.length(); i++) {
        double value = source.getElementAsDouble(i);
        if(Double.isNaN(value)) {
          extrema = value;
        } else if(!Double.isNaN(extrema)) {
          if(min) {
            if(value < extrema) {
              extrema = value;
            } 
          } else {
            if(value > extrema) {
              extrema = value;
            }
          }
        }
        result.add(extrema);
      }
    }
    return result.build();
  }


  private static IntVector cumulativeIntegerExtrema(Vector source, boolean min) {
    IntArrayVector.Builder result = new IntArrayVector.Builder(0, source.length());
    result.setAttribute(Symbols.NAMES, source.getNames());

    if(source.length() > 0) {
      int extrema = source.getElementAsInt(0);
      result.add(extrema);
      for (int i = 1; i < source.length(); i++) {
        int value = source.getElementAsInt(i);
        if(IntVector.isNA(value)) {
          extrema = value;
        } else if(!IntVector.isNA(extrema)) {
          if(min) {
            if(value < extrema) {
              extrema = value;
            }
          } else {
            if(value > extrema) {
              extrema = value;
            }
          }
        }
        result.add(extrema);
      }
    }
    return result.build();
  }
  
}
