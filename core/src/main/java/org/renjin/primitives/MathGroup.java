/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.renjin.primitives;

import com.google.common.math.IntMath;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.MathUtils;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double gamma(double x) {
    return Math.exp(Gamma.logGamma(x));
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double lgamma(double x) {
    return Gamma.logGamma(x);
  }

  
  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double digamma(double x) {
    return Gamma.digamma(x);
  }


  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double trigamma(double x) {
    // Handle edge cases consistently with GNU R
    if(Double.isNaN(x)) {
      return x;
    }
    if(x == Double.NEGATIVE_INFINITY) {
      return Double.POSITIVE_INFINITY;
    }
    return Gamma.trigamma(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double sign(double x) {
    return Math.signum(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double log(double x, double base) {

    //Method cannot be called directly as R and Apache Commons Math argument order
    // are reversed
    return MathUtils.log(base, x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double log(double d) {
    return Math.log(d);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double log2(double d) {
    return MathUtils.log(2, d);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double log10(double d) {
    return Math.log10(d);
  }
  
  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double sqrt(double x) {
    return Math.sqrt(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double sin(double x) {
    return Math.sin(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double sinh(double x) {
    return Math.sinh(x);
  }


  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double sinpi(double x) {
    if(Double.isInfinite(x) || Double.isNaN(x)) {
      return Double.NaN;
    }
    if(Math.floor(x) == x) {
      return 0d;
    }
    return Math.sin(x*Math.PI);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double asin(double val) {
    return Math.asin(val);
  }
  
  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double asinh(double val) {
    if(Double.isInfinite(val)) {
      return val;
    }
    return (Math.log(val + Math.sqrt(val * val + 1)));
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double cos(double val) {
    return Math.cos(val);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double cosh(double val) {
    return Math.cosh(val);
  }


  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double cospi(double x) {
    return Math.cos(x * Math.PI);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double acos(double val) {
    return Math.acos(val);
  }
  
  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double acosh(double val) {
    return (Math.log(val + Math.sqrt(val + 1) * Math.sqrt(val - 1)));
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double atanh(double val) {
    return (0.5 * Math.log((1 + val) / (1 - val)));
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double tan(double x) {
    return Math.tan(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double tanh(double x) {
    return Math.tanh(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double tanpi(double x) {
    return Math.tan(x * Math.PI);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double atan(double x) {
    return Math.atan(x);
  }
  
  @Deferrable
  @Internal
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double atan2(double y, double x) {
    return (Math.atan2(y, x));
  }


  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double signif(double x) {
    return signif(x, 6);
  }


  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double exp(double x) {
    return Math.exp(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double expm1(double x) {
    return Math.expm1(x);
  }

  @Deferrable
  @Builtin
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double log1p(double x) {
    return Math.log1p(x);
  }


  @Builtin
  @Deferrable
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double floor(double x) {
    return Math.floor(x);
  }
  
  @Builtin
  @Deferrable
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double ceiling(double x) {
    return Math.ceil(x);
  }
  
  
  @Builtin
  @Deferrable
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double round(double x) {
    return Math.rint(x);
  }
  
  @Builtin
  @Deferrable
  @DataParallel(PreserveAttributeStyle.ALL)
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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double trunc(double x) {
    if(x < 0) {
      return Math.ceil(x);
    } else {
      return Math.floor(x);
    }
  }


  @Builtin
  public static DoubleVector cumsum(DoubleVector source) {
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

  @Builtin
  public static DoubleVector cumsum(RawVector source) {
    return cumsum(Vectors.asDouble(source));
  }

  @Builtin
  public static IntVector cumsum(@Current Context context, IntVector source) {
    return cumulativeSumIntegers(source);
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
    return cumulativeSumIntegers(source);
  }

  @Builtin
  public static DoubleVector cumsum(StringVector source) {
    return cumsum(Vectors.asDouble(source));
  }

  private static IntVector cumulativeSumIntegers(Vector source) {
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
    return (result.build());
  }

  
  @Builtin
  public static DoubleVector cumprod(Vector source) {
    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder();
    result.setAttribute(Symbols.NAMES, source.getNames());

    if(source.length() > 0) {
      double sum = source.getElementAsDouble(0);
      result.add(sum);
      for (int i = 1; i < source.length(); i++) {
        sum *= source.getElementAsDouble(i);
        if (Double.isNaN(sum)) {
          result.addNA();
        } else {
          result.add(sum);
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
    return cumulativeComplex("cummin", source);
  }

  @Builtin
  public static ComplexVector cummax(ComplexVector source) {
    return cumulativeComplex("cummax", source);
  }
  
  private static ComplexVector cumulativeComplex(String functionName, ComplexVector source) {
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
