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
package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

public class OnExitFunction extends SpecialFunction {

  public OnExitFunction() {
    super("on.exit");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call) {
    EvalException.check(call.getArguments().length() <= 2,
        "invalid number of arguments");

    // If on.exit is evaluated from within a Promise, we may need to
    // crawl a few levels up to find the appropriate context to which
    // attach the exit handler
    Context exitContext = findMatchingContext(context, rho);

    if(call.getArguments().length() == 0) {
      // remove existing on exit functions
      exitContext.clearOnExits();

    } else {

      SEXP value = call.getArgument(0);
      boolean add = false;
      if(call.getArguments().length() == 2) {
        add = context.evaluate(call.getArgument(1), rho).asReal() != 0;
      }

      if(add) {
        exitContext.addOnExit(value);
      } else {
        exitContext.setOnExit(value);
      }
    }

    // Always return invisible()
    context.setInvisibleFlag();
    return Null.INSTANCE;
  }

  /**
   * Find the context matching the calling environment.
   */
  private Context findMatchingContext(Context context, Environment rho) {
    while(true) {
      if(context.getEnvironment() == rho) {
        return context;
      }
      if(context.getType() == Context.Type.TOP_LEVEL) {
        return context;
      }
      context = context.getParent();
    }
  }
}
