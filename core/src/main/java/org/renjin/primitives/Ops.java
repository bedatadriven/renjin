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

import jdk.nashorn.internal.ir.EmptyNode;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.sexp.*;
import org.renjin.sexp.SexpType;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.lang.Math.copySign;
import static org.renjin.sexp.DoubleVector.isFinite;
import static org.renjin.util.CDefines.*;


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
    int r = a + b;
    boolean bLTr = b < r;
    if (a > 0) {
      if (bLTr) {
        return r;
      }
    } else {
      if (!bLTr) {
        return r;
      }
    }
    return IntVector.NA;
  }

  @Builtin("+")
  @DataParallel(PreserveAttributeStyle.ALL)
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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex negative(Complex x) {
    return ComplexVector.complex(-x.getReal(), -x.getImaginary());
  }
  
  @Builtin("-")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex minus(Complex x, Complex y) {
    return x.subtract(y);
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
    int r = a - b;
    if ((a < 0 == b < 0) || (a < 0 == r < 0)) {
      return r;
    } else {
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
  @DataParallel(PreserveAttributeStyle.ALL)
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
    long l = (long) x * (long) y;
    if (!(l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)) {
      return (int) l;
    } else {
      return IntVector.NA;
    }
  }


  @Builtin("*")
  @DataParallel(PreserveAttributeStyle.ALL)
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
  public static boolean equalTo(double x, double y) {
    return x == y;
  }

  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo(Complex x, Complex y) {
    return x.equals(y);
  }


  /*
  * Relational operators in R are in do_relop function within relop.c file
  * do_relop handles all cases where NA has to be returned, find whether Real or Integer
  * functions should be used, and calls 'de_relop_dflt()'. Here Scalar cases are handeled
  * by dispatch to DO_SCALAR_RELOP() macros, and list/vector input is coerced by calling
  * coerceVector() function located within coerce.c
  * coerceVector() function handles different SEXP types by calling the following functions:
  *
  * Type        Function
  * SYMSXP      coerceSymbol()
  * NILSXP      coercePairList()
  * LISTSXP     coercePairList()
  * LANGSXP     coercePairList()
  * VECSXP      coerceVectorList()
  * EXPRSXP     coerceVectorList()
  * ENVSXP      error "environments cannot be coerced to other types"
  * LGLSXP      COERCE_ERROR macro "cannot coerce type '%s' to vector of type '%s'"
  * INTSXP      COERCE_ERROR macro
  * REALSXP     COERCE_ERROR macro
  * CPLSXP      COERCE_ERROR macro
  * STRSXP      COERCE_ERROR macro
  * RAWSXP      COERCE_ERROR macro
  *
  * coercePairList() calls PairToVectorList() to convert a PairList to a List
  */

  public static SEXP coerceSymbol(SEXP vector, int type) {
    SEXP rval;
      switch(type) {
          case SexpType.EXPRSXP:
              ListVector.Builder listVector = new ListVector.Builder(1);
              rval = listVector.add(vector).build();
              break;
          case SexpType.CHARSXP:
          case SexpType.STRSXP:
              StringVector.Builder stringVector = new StringVector.Builder(1);
              rval = stringVector.add(vector.getNames()).build();
              break;
          default:
              throw new EvalException("(symbol) object cannot be coerced to type " + type2char(type));
      };
      return rval;
  }

  public static SEXP PairToVectorList(SEXP vector) {
      // Should convert a PairList to a List and copies all the attributes including TAGs

      return vector;
  }

  public static SEXP coercePairlist(SEXP vectors, int type) {
      int i, n = 0;
      SEXP rval = null, vp, names;

      if (type == SexpType.LISTSXP) {
          return vectors;
      }

      names = vectors;

      if (type == SexpType.EXPRSXP) {
          StringVector.Builder stringVector = new StringVector.Builder(1);
          rval = stringVector.add(vectors.getNames()).build();
          return rval;

      } else if (type == SexpType.STRSXP) {
          n = vectors.length();
          StringVector.Builder stringVector = new StringVector.Builder(n);
          for(vp = vectors, i = 0; vp != ListVector.EMPTY; vp = CDR(vp), i++) {
            if (isString(CAR(vp)) && CAR(vp).length() == 1) {
                stringVector.add(STRING_ELT(CAR(vp), 0));
            } else {
                stringVector.add(STRING_ELT(deparse1line(CAR(vp), 0), 0));
            }
          }

      } else if (type == SexpType.VECSXP) {
          /* */
          rval = PairToVectorList(vectors);
          return rval;

      } else if (isVectorizable(vectors)) {

          switch(type) {
              case SexpType.LGLSXP:
                  // for (i = 0, vp = v; i < n; i++, vp = CDR(vp))
                  // LOGICAL(rval)[i] = asLogical(CAR(vp));
                  break;
              case: SexpType.INTSXP:

                  break;
              case: SexpType.REALSXP:

                  break;
              case: SexpType.CPLXSXP:

                  break;
              case: SexpType.RAWSXP:

                  break;
              default: throw new EvalException("UNIMPLEMENTED_TYPE");
          }

      } else {
          throw new EvalException("'pairlist' object cannot be coerced to type " + type2char(type));
      }

      // use names (from PRINTNAME(TAG()) ) to setAttrib(rval, R_NamesSymbol, names)
      //return rval;
  }


  @Builtin("==")
  @DataParallel
  public static SEXP equalTo(ListVector x, SEXP y) {
    LogicalArrayVector.Builder res = new LogicalArrayVector.Builder();
    int yLength = y.length();
    int yIndex = 0;

    if (yLength > 1) {
      for (SEXP xElement : x) {
        if (yIndex == yLength) {
          yIndex = 0;
          res.add(xElement.toString().equals(y.getElementAsSEXP(yIndex).toString()));
          yIndex += 1;
        }
      }
    } else {
      for (SEXP xElement : x) {
        res.add(xElement.toString().equals(y.toString()));
      }
    }

    return res.build().getElementAsSEXP(0);
  }
  
  @Deferrable
  @Builtin("==")
  @DataParallel
  public static boolean equalTo(@CoerceLanguageToString String x, @CoerceLanguageToString String y) {
    return x.equals(y);
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
  public static boolean notEqualTo(@CoerceLanguageToString String x, @CoerceLanguageToString String y) {
    return !x.equals(y);
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
  public static boolean lessThan(@CoerceLanguageToString String x, @CoerceLanguageToString String y) {
    return x.compareTo(y) < 0;
  }

  @Builtin("<")
  @DataParallel
  public static boolean lessThan(Complex x, Complex y) {
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
  public static boolean lessThanOrEqualTo(@CoerceLanguageToString String x, @CoerceLanguageToString String y) {
    return x.compareTo(y) <= 0;
  }

  @Builtin("<=")
  @DataParallel
  public static boolean lessThanOrEqualTo(Complex x, Complex y) {
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
  public static boolean greaterThan(@CoerceLanguageToString String x, @CoerceLanguageToString String y) {
    return x.compareTo(y) > 0;
  }

  @Builtin(">")
  @DataParallel
  public static boolean greaterThan(Complex x, Complex y) {
    throw new EvalException("invalid comparison with complex values");
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(double x, double y) {
    return x >= y;
  }

  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(Complex x, Complex y) {
    throw new EvalException("invalid comparison with complex values");
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(@CoerceLanguageToString String x, @CoerceLanguageToString String y) {
    return x.compareTo(y) >= 0;
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
    if (b != 0) {
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
