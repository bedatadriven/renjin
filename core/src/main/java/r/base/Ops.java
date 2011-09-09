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

package r.base;

import r.jvmi.annotations.AllowNA;
import r.jvmi.annotations.GroupGeneric;
import r.jvmi.annotations.PreserveAttributeStyle;
import r.jvmi.annotations.PreserveAttributes;
import r.jvmi.annotations.Primitive;
import r.lang.DoubleVector;
import r.lang.Logical;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.Vector;
import r.lang.exception.EvalException;

/**
 * Default implementations of the Ops group of functions.
 */
@GroupGeneric
public class Ops  {

  private Ops() {}

  @Primitive("+")
  @PreserveAttributes(PreserveAttributeStyle.ALL)
  public static double plus(double x, double y) {
    return x + y;
  }

  @Primitive("+")
  @PreserveAttributes(PreserveAttributeStyle.ALL)
  public static double plus(double x) {
    return x;
  }

  @Primitive("-")
  @PreserveAttributes(PreserveAttributeStyle.ALL)
  public static double minus(double x, double y) {
    return x - y;
  }

  @Primitive("-")
  @PreserveAttributes(PreserveAttributeStyle.ALL)
  public static double minus(double x) {
    return -x;
  }

  @Primitive("/")
  @PreserveAttributes(PreserveAttributeStyle.ALL)
  public static double divide(double x, double y) {
    return x / y;
  }

  @Primitive("*")
  @PreserveAttributes(PreserveAttributeStyle.ALL)
  public static double multiply(double x, double y) {
    return x * y;
  }

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

  @Primitive("^")
  @PreserveAttributes(PreserveAttributeStyle.ALL)
  public static double power(double x, double y) {
    return Math.pow(x, y);
  }

  @Primitive("!")
  public static boolean not(boolean value) {
    return !value;
  }

  @Primitive("&")
  @AllowNA
  public static Logical and(double x, double y) {
    if(x == 0 || y == 0) {
      return Logical.FALSE;
    } else if(DoubleVector.isNA(x) || DoubleVector.isNA(y)) {
      return Logical.NA;
    } else {
      return Logical.TRUE;
    }
  }

  @Primitive("|")
  @AllowNA
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
  
  @Primitive("%*%")
  public static SEXP matrixproduct(Vector v1, Vector v2) {
    DoubleVector.Builder b = new DoubleVector.Builder();
    if(v1.getAttribute(Symbol.DIM) instanceof r.lang.Null){
      if(v2.getAttribute(Symbol.DIM) instanceof r.lang.Null){
        double sum = 0.0;
        for (int i=0;i<v1.length();i++){
          sum+=v1.getElementAsDouble(i) * v2.getElementAsDouble(i);
        }
        b.add(sum);
        return(b.build());
      }
    }
    
    throw new EvalException("Only vector dot product implemented.");
  }
  
}
