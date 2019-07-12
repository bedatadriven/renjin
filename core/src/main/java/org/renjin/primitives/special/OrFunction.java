/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.sexp.*;

/**
 * The logical || operator requires the implementation use minimal evaluation,
 * therefore we cannot use the overloaded function calls as is standard.
 * <p>
 * Comparing doubles or booleans works as generally expected. Comparing two vectors
 * will only compare the first element in each vector.
 */
public class OrFunction extends SpecialFunction {

  public OrFunction() {
    super("||");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call) {
    checkArity(call, 2);

    int x = AndFunction.test(context.evaluate(call.getArgument(0), rho), "invalid 'x' type in 'x || y'");

    boolean xna = x == LogicalVector.NA;

    if (x != 0 && !xna) {
      return LogicalVector.TRUE;
    }

    int y = AndFunction.test(context.evaluate(call.getArgument(1), rho), "invalid 'y' type in 'x || y'");
    boolean yna = y == LogicalVector.NA;

    if (y != 0 && !yna) {
      return LogicalVector.TRUE;
    }

    if (xna || yna) {
      return LogicalVector.NA_VECTOR;

    } else {
      return LogicalVector.FALSE;
    }
  }
}
