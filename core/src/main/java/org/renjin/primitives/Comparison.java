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

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.sexp.*;


public class Comparison {

  private Comparison() { }


  /**
   * The logical || operator reqires the implementation use minimal evaluation,
   * therefore we cannot use the overloaded function calls as is standard.
   *
   * Comparing doubles or booleans works as generally expected. Comparing two vectors
   * will only compare the first element in each vector.
   */
  @Builtin("||")
  public static LogicalVector or(@Current Context context, @Current Environment rho,
                                 @Unevaluated SEXP xExp,
                                 @Unevaluated SEXP yExp) {

    Logical x = checkedToLogical(context.evaluate(xExp, rho), "invalid 'x' type in 'x || y'");

    if(x == Logical.TRUE) {
      return LogicalVector.TRUE;
    }

    Logical y = checkedToLogical(context.evaluate(yExp, rho), "invalid 'y' type in 'x || y'");
    if(y == Logical.TRUE) {
      return LogicalVector.TRUE;
    }

    if(x == Logical.NA || y == Logical.NA) {
      return LogicalVector.NA_VECTOR;
    } else {
      return LogicalVector.FALSE;
    }
  }

  /**
   * The logical && operator requires the implementation use minimal evaluation,
   * therefore we cannot use the overloaded function calls as is standard.
   *
   * Comparing doubles or booleans works as generally expected. Comparing two vectors
   * will only compare the first element in each vector.
   */
  @Builtin("&&")
  public static LogicalVector and(@Current Context context, @Current Environment rho,
                                  @Unevaluated SEXP xExp,
                                  @Unevaluated SEXP yExp) {

    Logical x = checkedToLogical(context.evaluate(xExp, rho), "invalid 'x' type in 'x && y'");

    if(x == Logical.FALSE) {
      return LogicalVector.FALSE;
    }

    Logical y = checkedToLogical(context.evaluate(yExp, rho), "invalid 'y' type in 'x && y'");

    if(y == Logical.FALSE) {
      return LogicalVector.FALSE;
    } else if(x == Logical.TRUE && y == Logical.TRUE) {
      return LogicalVector.TRUE;
    } else {
      return LogicalVector.NA_VECTOR;
    }
  }


  private static Logical checkedToLogical(SEXP exp, String errorMessage) {
    if(exp instanceof AtomicVector) {
      AtomicVector vector = (AtomicVector) exp;
      if(vector.length() == 0) {
        return Logical.NA;
      } else {
        return vector.getElementAsLogical(0);
      }
    }
    throw new EvalException(errorMessage);
  }
}
