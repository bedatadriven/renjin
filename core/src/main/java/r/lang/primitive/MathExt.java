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

import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.ListVector;
import r.lang.SEXP;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.ArgumentList;
import r.lang.primitive.annotations.NamedFlag;

/**
 * Math functions not found in java.Math or apache commons math
 */
public class MathExt {

  public static double plus(double x, double y) {
    return x + y;
  }

  public static double plus(double x) {
    return x;
  }

  public static double minus(double x, double y) {
    return x - y;
  }

  public static double minus(double x) {
    return -x;
  }

  public static double divide(double x, double y) {
    return x / y;
  }

  public static double multiply(double x, double y) {
    return x * y;
  }

  public static double prod(@ArgumentList ListVector arguments, @NamedFlag("na.rm") boolean removeNA) {
    double product = 1;
    for(SEXP argument : arguments) {
      AtomicVector vector = EvalException.checkedCast(argument);
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          if(!removeNA) {
            return DoubleVector.NA;
          }
        } else {
          product = product * vector.getElementAsDouble(i);
        }
      }
    }
    return product;
  }
}
