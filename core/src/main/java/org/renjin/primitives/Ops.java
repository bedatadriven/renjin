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

import org.apache.commons.math.complex.Complex;
import org.renjin.invoke.annotations.*;
import org.renjin.invoke.annotations.DataParallel;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.Vector;


/**
 * Default implementations of the Ops group of functions.
 */
@GroupGeneric
public class Ops  {

  private Ops() {}

  @Deferrable
  @Builtin("+")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double plus(double x, double y) {
    return x + y;
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
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double minus(double x, double y) {
    return x - y;
  }
  
  @Builtin("-")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex negative(Complex x) {
    return new Complex(-x.getReal(), x.getImaginary());
  }
  
  @Builtin("-")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex minus(Complex x, Complex y) {
    return x.subtract(y);
  }

  @Deferrable
  @Builtin("-")
  @DataParallel(PreserveAttributeStyle.ALL)
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
  @Builtin("/")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double divide(double x, double y) {
    return x / y;
  }

  @Builtin("/")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex divide(Complex x, Complex y) {
    return x.divide(y);
  }

  
  @Deferrable
  @Builtin("*")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double multiply(double x, double y) {
    return x * y;
  }

  @Builtin("*")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static Complex multiply(Complex x, Complex y) {
    return x.multiply(y);
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

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(double x, double y) {
    return x >= y;
  }

  @Deferrable
  @Builtin(">=")
  @DataParallel
  public static boolean greaterThanOrEqual(@CoerceLanguageToString String x, @CoerceLanguageToString String y) {
    return x.compareTo(y) >= 0;
  }

  @Deferrable
  @Builtin("^")
  @DataParallel(PreserveAttributeStyle.ALL)
  public static double power(double x, double y) {
    return Math.pow(x, y);
  }

  @Deferrable
  @Builtin("!")
  @DataParallel
  public static boolean not(boolean value) {
    return !value;
  }

  @Deferrable
  @Builtin("%%")
  @DataParallel
  public static double modulus(double x, double y) {
    return x % y;
  }

  @Deferrable
  @Builtin("%/%")
  @DataParallel
  public static double integerDivision(double x, double y) {
    return Math.floor(x / y);
  }

  @Deferrable
  @Builtin("&")
  @DataParallel(passNA = true)
  public static Logical and(double x, double y) {
    if(x == 0 || y == 0) {
      return Logical.FALSE;
    } else if(DoubleVector.isNA(x) || DoubleVector.isNA(y)) {
      return Logical.NA;
    } else {
      return Logical.TRUE;
    }
  }

  @Deferrable
  @Builtin("|")
  @DataParallel(passNA = true)
  public static Logical or(double x, double y) {
    if( (x != 0 && !DoubleVector.isNA(x)) ||
        (y != 0 && !DoubleVector.isNA(y))) {
      return Logical.TRUE;
    } else if(x == 0 && y == 0) {
      return Logical.FALSE;
    } else {
      return Logical.NA;
    }
  }
}
