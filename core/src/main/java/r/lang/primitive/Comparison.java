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

package r.lang.primitive;

import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.AllowNA;
import r.lang.primitive.annotations.ArgumentList;
import r.lang.primitive.annotations.NamedFlag;
import r.lang.primitive.annotations.Primitive;

public class Comparison {


  @Primitive("==")
  public static boolean equalTo(double x, double y) {
    return x == y;
  }

  @Primitive("==")
  public static boolean equalTo(String x, String y) {
    return x.equals(y);
  }

  @Primitive("!=")
  public static boolean notEqualTo(double x, double y) {
    return x != y;
  }

  @Primitive("!=")
  public static boolean notEqualTo(String x, String y) {
    return !x.equals(y);
  }

  @Primitive("<")
  public static boolean lessThan(double x, double y) {
    return x < y;
  }

  @Primitive("<")
  public static boolean lessThan(String x, String y) {
    return x.compareTo(y) < 0;
  }

  @Primitive("<=")
  public static boolean lessThanOrEqualTo(double x, double y) {
    return x <= y;
  }

  @Primitive("<=")
  public static boolean lessThanOrEqualTo(String x, String y) {
    return x.compareTo(y) <= 0;
  }

  @Primitive(">")
  public static boolean greaterThan(double x, double y) {
    return x > y;
  }

  @Primitive(">")
  public static boolean greaterThan(String x, String y) {
    return x.compareTo(y) > 0;
  }

  @Primitive(">=")
  public static boolean greaterThanOrEqual(double x, double y) {
    return x >= y;
  }

  @Primitive(">=")
  public static boolean greaterThanOrEqual(String x, String y) {
    return x.compareTo(y) >= 0;
  }

  /**
   * The logical || operator reqires the implementation use minimal evaluation,
   * therefore we cannot use the overloaded function calls as is standard.
   *
   * Comparing doubles or booleans works as generally expected. Comparing two vectors
   * will only compare the first element in each vector.
   */
  public static Logical or(Context context, Environment rho, FunctionCall call) {

    Logical x = checkedToLogical(call.evalArgument(context, rho, 0), "invalid 'x' type in 'x || y'");

    if(x == Logical.TRUE) {
      return x;
    }

    Logical y = checkedToLogical(call.evalArgument(context, rho, 1), "invalid 'y' type in 'x || y'");
    if(y == Logical.TRUE) {
      return y;
    }

    if(x == Logical.NA || y == Logical.NA) {
      return Logical.NA;
    } else {
      return Logical.FALSE;
    }
  }

  @Primitive("|")
  @AllowNA
  public static Logical bitwiseOr(double x, double y) {
    if( (x != 0 && !DoubleVector.isNA(x)) ||
        (y != 0 && !DoubleVector.isNA(y))) {
      return Logical.TRUE;
    } else if(x == 0 && y == 0) {
      return Logical.FALSE;
    } else {
      return Logical.NA;
    }
  }

  /**
   * The logical && operator requires the implementation use minimal evaluation,
   * therefore we cannot use the overloaded function calls as is standard.
   *
   * Comparing doubles or booleans works as generally expected. Comparing two vectors
   * will only compare the first element in each vector.
   */
  @Primitive("&&")
  public static Logical and(Context context, Environment rho, FunctionCall call) {

    Logical x = checkedToLogical(call.evalArgument(context, rho, 0), "invalid 'x' type in 'x && y'");

    if(x == Logical.FALSE) {
      return Logical.FALSE;
    }

    Logical y = checkedToLogical(call.evalArgument(context, rho, 1), "invalid 'y' type in 'x && y'");

    if(y == Logical.FALSE) {
      return Logical.FALSE;
    } else if(x == Logical.TRUE && y == Logical.TRUE) {
      return Logical.TRUE;
    } else {
      return Logical.NA;
    }
  }

  @Primitive("&")
  @AllowNA
  public static Logical bitwiseAnd(double x, double y) {
    if(x == 0 || y == 0) {
      return Logical.FALSE;
    } else if(DoubleVector.isNA(x) || DoubleVector.isNA(y)) {
      return Logical.NA;
    } else {
      return Logical.TRUE;
    }
  }

  @Primitive("!")
  public static boolean not(boolean value) {
    return !value;
  }

  public static Logical any(@ArgumentList ListVector arguments,
                            @NamedFlag("na.rm") boolean removeNA) {

    for(SEXP argument : arguments) {
      Vector vector = (Vector) argument;
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if(!removeNA) {
            return Logical.NA;
          }
        } else if(vector.getElementAsDouble(i) != 0) {
          return Logical.TRUE;
        }
      }
    }
    return Logical.FALSE;
  }


  public static Logical all(@ArgumentList ListVector arguments,
                            @NamedFlag("na.rm") boolean removeNA) {

    for(SEXP argument : arguments) {
      Vector vector = (Vector) argument;
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if(!removeNA) {
            return Logical.NA;
          }
        } else if(vector.getElementAsDouble(i) == 0) {
          return Logical.FALSE;
        }
      }
    }
    return Logical.TRUE;
  }

  private static Logical checkedToLogical(SEXP exp, String errorMessage) {
    if(exp instanceof AtomicVector) {
      AtomicVector vector = (AtomicVector) exp;
      if(vector.length() > 0) {
        return vector.getElementAsLogical(0);
      }
    }
    throw new EvalException(errorMessage);
  }
}
