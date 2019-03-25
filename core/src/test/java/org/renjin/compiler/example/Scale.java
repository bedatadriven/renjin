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

package org.renjin.compiler.example;

import org.renjin.eval.Context;
import org.renjin.eval.Support;
import org.renjin.primitives.R$primitive$$eq$eq;
import org.renjin.primitives.special.IfFunction;
import org.renjin.sexp.*;

import java.util.Arrays;

public class Scale {

  /**
   *
   * @param context
   * @param parentEnvironment
   * @param arguments the arguments to this function, matched by name.
   * @return
   * @throws Exception
   */
  public SEXP scale(Context context, Environment parentEnvironment, SEXP[] arguments) throws Exception {

    // PARAMETERS VARIABLES
    int X = 0;
    int CENTER = 1;
    int SCALE = 2;

    if(arguments[CENTER] == null) {
      arguments[CENTER] = LogicalVector.TRUE;
    }

    if(arguments[SCALE] == null) {
      arguments[SCALE] = LogicalVector.TRUE;
    }

    // Local variables
    int NC = 3;

    SEXP[] array = Arrays.copyOf(arguments, 12);

    // Create function's environment
    LocalEnvironment frame = new LocalEnvironment(parentEnvironment, array);

    // Start....


    //scale.default <- function(x, center = TRUE, scale = TRUE)

    frame.set(X, invoke(frame, "as.matrix", frame.get(X)));
    frame.set(NC, invoke(frame,"ncol", frame.get(X)));

    SEXP t1 = invoke(frame, "is.logical", frame.get(CENTER));

    // L0   5:  if center => TRUE:L3, FALSE:L2, NA:ERROR
    if(IfFunction.asLogicalNoNA(context, null, t1)) {
      //L3 center ← colMeans(x, na.rm = TRUE)
      frame.set(CENTER, invoke2(frame, "colMeans",
          null, frame.promise(X),
          "na.rm", LogicalVector.TRUE));

      //     7:  x ← sweep(x, c(2L), center, check.margin = FALSE)
      frame.set(X, invoke3(frame, "sweep",
          null, frame.promise(X),
          null, frame.promise(CENTER),
          "check.margin", LogicalVector.FALSE));
    } else {
      /// L1   9:  T3 ← (is.numeric center)
      SEXP t3 = invoke(frame, "is.numeric", frame.get(CENTER));
      SEXP t4 = invoke(frame, "length", frame.get(CENTER));
      SEXP t5 = R$primitive$$eq$eq.doApply(context, frame, t4, frame.get(NC));
      if (Support.test(t3) && Support.test(t5)) {
        // L11  22: x ← sweep(x, c(2L), center, check.margin = FALSE)
        frame.set(X, invoke4(frame, "sweep",
            null, frame.promise(X),
            null, IntVector.valueOf(2),
            null, frame.promise(CENTER),
            "check.margin", LogicalVector.FALSE));

      } else {
        invoke(frame, "stop", StringVector.valueOf("length of 'center' must equal the number of columns of 'x'"));
      }
    }

    //L2:

    throw new UnsupportedOperationException();
  }

  private SEXP invoke(Environment rho, String functionName, SEXP x) {
    return null;
  }

  private static SEXP invoke2(Environment rho, String functionName, String argName0, SEXP arg0, String argName1, SEXP arg1) {
    return null;
  }

  private static SEXP invoke3(Environment rho, String functionName,
                              String argName0, SEXP arg0,
                              String argName1, SEXP arg1,
                              String argName2, SEXP arg2) {
    throw new UnsupportedOperationException();
  }

  private static SEXP invoke4(Environment rho, String functionName,
                              String argName0, SEXP arg0,
                              String argName1, SEXP arg1,
                              String argName2, SEXP arg2,
                              String argName3, SEXP arg3) {
    throw new UnsupportedOperationException();
  }

}
