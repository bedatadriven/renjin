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

import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.NamedFlag;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.lang.exception.EvalException;

public class Comparison {


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
